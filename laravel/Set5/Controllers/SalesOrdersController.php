<?php

namespace App\Http\Controllers\API\CustomersAndSales;

use App\Http\Controllers\BaseController;
use App\Mail\Services\CustomersAndSales\SalesOrderMailService;
use App\Models\CustomersAndSales\Customer\Customer;
use App\Models\CustomersAndSales\DeliveryTerm;
use App\Models\CustomersAndSales\PaymentTerm;
use App\Models\CustomersAndSales\SalesOrder\SalesOrderLine;
use App\Models\CustomersAndSales\SalesPerson;
use App\Models\CustomersAndSales\SalesPriceList\SalesPriceList;
use App\Models\InventoryManagement\Item\Item;
use App\Models\InventoryManagement\Site;
use App\Models\Tax;
use App\Models\TaxGroup;
use App\Services\CustomersAndSales\SalesOrderService;
use App\Support\LineItemCollection;
use App\Support\LineItemDiscountTypes;
use Illuminate\Support\Arr;
use Illuminate\Validation\NestedRules;
use Illuminate\Validation\Rule;
use App\Models\InventoryManagement\Item\ItemUnitOfMeasurement;

class SalesOrdersController extends BaseController
{
    protected $service = SalesOrderService::class;

    protected $emailService = SalesOrderMailService::class;

    protected function createRules(): array
    {
        return [
            'order_date' => [
                'required',
                'date_format:Y-m-d',
                'after_or_equal:1970-01-01',
                'before_or_equal:2999-12-31',
            ],
            'requested_delivery_date' => [
                'required',
                'date_format:Y-m-d',
                'before_or_equal:2999-12-31',
                'after_or_equal:order_date',
            ],
            'purchase_order_date' => [
                'nullable',
                'date_format:Y-m-d',
                'after_or_equal:1970-01-01',
                'before_or_equal:2999-12-31',
            ],
            'purchase_order_number' => [
                'nullable',
                'string',
                'max:255',
            ],
            'description' => [
                'nullable',
                'string',
                'max:255',
            ],
            'sales_person_id' => [
                'required',
                'integer',
                Rule::exists(SalesPerson::class, 'id')
            ],
            'payment_term_id' => [
                'required',
                'integer',
                Rule::exists(PaymentTerm::class, 'id')
            ],
            'delivery_term_id' => [
                'required',
                'integer',
                Rule::exists(DeliveryTerm::class, 'id')
            ],
            'customer_id' => [
                'required',
                'integer',
                Rule::exists(Customer::class, 'id')
            ],
            'customer_site_id' => [
                'required',
                'integer',
                Rule::exists(Site::class, 'id')
                    ->where('siteable_type', Customer::class)
            ],
            'sales_price_list_id' => [
                'nullable',
                'integer',
                Rule::exists(SalesPriceList::class, 'id')
            ],
            'site_id' => [
                'required',
                'integer',
                Rule::exists(Site::class, 'id'),
            ],
            'promised_delivery_date' => [
                'nullable',
                'date_format:Y-m-d',
                'after_or_equal:1970-01-01',
                'before_or_equal:2999-12-31',
            ],

            'currency_id' => [
                'required',
                'alpha',
                'max:3',
                Rule::in(array_keys(config('currencies')))
            ],
            'exchange_rate' => [
                'required',
                'numeric',
                'min:0',
                'max:2147483647'
            ],

            'lines' => [
                'required',
                'array',
                'min:1'
            ],
            'lines.data' => [
                'required',
                'array',
                'min:1'
            ],
            'lines.data.*' => [
                'required',
                'array'
            ],
            'lines.data.*.line' => [
                'required',
                'integer',
                'distinct',
            ],
            'lines.data.*.item_id' => $this->handleValidationLogicForLineItems([
                'integer',
                'distinct',
                Rule::exists(Item::class, 'id')
                    ->where('can_sell', true),
            ]),
            'lines.data.*.description' => $this->handleValidationLogicForLineItems([
                'nullable',
            ]),
            'lines.data.*.quantity' => $this->handleValidationLogicForLineItems([
                'required',
                'numeric',
                'gt:0',
                'lt:' . pow(10, 11)
            ]),
            'lines.data.*.rate' => $this->handleValidationLogicForLineItems([
                'required',
                'numeric',
                'gte:0',
                'lt:' . pow(10, 11)
            ]),
            'lines.data.*.discount_value' => $this->handleValidationLogicForLineItems([
                'nullable',
                'numeric',
                'gte:0',
                'lt:' . pow(10, 11)
            ]),
            'lines.data.*.discount_type' => $this->handleValidationLogicForLineItems([
                'required',
                'string',
                Rule::in(collect(LineItemDiscountTypes::cases())->pluck('value')->toArray()),
            ]),
            'lines.data.*.tax_group_id' => $this->handleValidationLogicForLineItems([
                'nullable',
                'integer',
                Rule::exists(TaxGroup::class, 'id')
            ]),
            'lines.data.*.item_uom_id' => $this->handleValidationLogicForLineItems([
                'required',
                'integer',
                Rule::exists(ItemUnitOfMeasurement::class, 'id'),
            ]),
            'lines.data.*.their_code' => $this->handleValidationLogicForLineItems([
                'nullable',
                'string',
                'max:255',
            ]),
            'memo' => [
                'nullable',
                'string',
                'max:255'
            ],
            'internal_note' => [
                'nullable',
                'string',
                'max:255'
            ],
        ];
    }

    protected function updateRules($id): array
    {
        return array_merge(
            $this->createRules(),
            [
                'lines.data.*.id' => [
                    'nullable',
                    'integer',
                    'gt:0',
                    Rule::exists(SalesOrderLine::class, 'id')
                        ->where('sales_order_id', $id),
                ],
            ]
        );
    }

    private function handleValidationLogicForLineItems($rules): NestedRules
    {
        return handleValidationRulesGenerationLogicForLineItems($rules);
    }

    protected function attributes(): array
    {
        return [
            'customer_id' => 'customer',
            'customer_site_id' => 'customer site',
            'payment_term_id' => 'payment term',
            'delivery_term_id' => 'delivery term',
            'sales_price_list_id' => 'price list',
            'sales_person_id' => 'sales person',
            'site_id' => 'site',
            'lines.data.*.item_id' => 'item',
            'lines.data.*.tax_group_id' => 'tax group',
            'lines.data.*.description' => 'description',
            'lines.data.*.quantity' => 'quantity',
            'lines.data.*.their_code' => 'their code',
            'lines.data.*.rate' => 'rate',
            'lines.data.*.discount_value' => 'discount value',
            'lines.data.*.discount_type' => 'discount type',
        ];
    }

    protected function afterValidation($data)
    {
        $lines = collect(Arr::get($data, 'lines.data'))
            ->filter(fn($l) => !empty(Arr::get($l, 'item_id')));

        $withholdingTax = Tax::find(Arr::get($data, 'withholding_tax_id'));

        (new LineItemCollection(
            items: $lines,
            currency: Arr::get($data, 'currency_id'),
            exchange_rate: Arr::get($data, 'exchange_rate'),
            withholding_tax: $withholdingTax
        ))
        ->validateLineItems();
    }
}
