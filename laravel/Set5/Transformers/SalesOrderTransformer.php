<?php

namespace App\Transformers\CustomersAndSales\SalesOrder;

use App\Models\CustomersAndSales\SalesOrder\SalesOrder;
use App\Transformers\Accounting\TaxTransformer;
use App\Transformers\CustomersAndSales\Customer\CustomerSiteTransformer;
use App\Transformers\CustomersAndSales\Customer\CustomerTransformer;
use App\Transformers\CustomersAndSales\DeliveryTermTransformer;
use App\Transformers\CustomersAndSales\PaymentTermTransformer;
use App\Transformers\CustomersAndSales\SalesPersonTransformer;
use App\Transformers\CustomersAndSales\SalesPriceList\SalesPriceListTransformer;
use League\Fractal\Resource\Collection;
use League\Fractal\Resource\Item;
use League\Fractal\TransformerAbstract;
use App\Transformers\InventoryManagement\SiteTransformer;

class SalesOrderTransformer extends TransformerAbstract
{
    /**
     * List of resources to automatically include
     *
     * @var array
     */
    protected array $defaultIncludes = [

    ];

    /**
     * List of resources possible to include
     *
     * @var array
     */
    protected array $availableIncludes = [
        'lines',
        'total_taxes',
        'customer',
        'customer_site',
        'sales_person',
        'sales_price_list',
        'payment_term',
        'delivery_term',
        'withholding_tax',
        'site',
        'delivery_schedules',
    ];

    /**
     * A Fractal transformer.
     *
     * @return array
     */
    public function transform($data)
    {
        $currency = $data?->currency_id ?? getBaseCurrency();

        return [
            'id' => $data?->id,
            'customer_id' => $data?->customer_id,
            'customer_site_id' => $data?->customer_site_id,
            'delivery_term_id' => $data?->delivery_term_id,
            'payment_term_id' => $data?->payment_term_id,
            'sales_contract_id' => $data?->sales_contract_id,
            'sales_person_id' => $data?->sales_person_id,
            'sales_price_list_id' => $data?->sales_price_list_id,
            'withholding_tax_id' => $data?->withholding_tax_id,
            'site_id' => $data?->site_id,

            'named_id' => $data?->named_id,
            'purchase_order_number' => $data?->purchase_order_number,
            'status' => $data?->status,
            'memo' => $data?->memo,
            'internal_note' => $data?->internal_note,
            'description' => $data?->description,

            'currency' => config("currencies.$currency"),
            'currency_id' => $currency,
            'currency_code' => $currency,
            'exchange_rate' => number_format($data?->exchange_rate ?? 1, 6, '.', ''),
            'gross_amount' => formatDecimal(fromMinor($data?->gross_amount ?? 0)),
            'gross_amount_bc' => formatDecimal(fromMinor($data?->gross_amount_bc ?? 0)),
            'discount_amount' => formatDecimal(fromMinor($data?->discount_amount ?? 0)),
            'discount_amount_bc' => formatDecimal(fromMinor($data?->discount_amount_bc ?? 0)),
            'net_amount' => formatDecimal(fromMinor($data?->net_amount ?? 0)),
            'net_amount_bc' => formatDecimal(fromMinor($data?->net_amount_bc ?? 0)),
            'tax_amount' => formatDecimal(fromMinor($data?->tax_amount ?? 0)),
            'tax_amount_bc' => formatDecimal(fromMinor($data?->tax_amount_bc ?? 0)),
            'total_amount' => formatDecimal(fromMinor($data?->total_amount ?? 0)),
            'total_amount_bc' => formatDecimal(fromMinor($data?->total_amount_bc ?? 0)),
            'withholding_tax_amount' => formatDecimal(fromMinor($data?->withholding_tax_amount ?? 0)),
            'withholding_tax_amount_bc' => formatDecimal(fromMinor($data?->withholding_tax_amount_bc ?? 0)),
            'grand_total_amount' => formatDecimal(fromMinor($data?->grand_total_amount ?? 0)),
            'grand_total_amount_bc' => formatDecimal(fromMinor($data?->grand_total_amount_bc ?? 0)),
            'completed_at' => $data?->completed_at?->toDateString(),
            'completed_at_formatted' => formatDate($data?->completed_at),
            'posted_at' => $data?->created_at?->toDateTimeString(),
            'posted_at_formatted' => formatDateTime($data?->posted_at),
            'closed_at' => $data?->closed_at?->toDateTimeString(),
            'closed_at_formatted' => formatDateTime($data?->closed_at),

            'shipping_status' => $data?->shipping_status,
            'sales_items' => 'In Stock',
            'ingredients' => 'In Stock',
            'production' => 'Not Started',

            'created_at' => $data?->created_at?->toDateTimeString(),
            'updated_at' => $data?->updated_at?->toDateTimeString(),
        ];
    }

    public function includeLines(?SalesOrder $model): Collection
    {
        return $this->collection($model?->lines, new SalesOrderLineTransformer());
    }

    public function includeTotalTaxes(?SalesOrder $model): Collection
    {
        return $this->collection($model?->total_taxes, new SalesOrderTotalTaxTransformer());
    }

    public function includeCustomer(?SalesOrder $model): Item
    {
        return $this->item($model?->customer, new CustomerTransformer());
    }

    public function includeCustomerSite(?SalesOrder $model): Item
    {
        return $this->item($model?->customer_site, new CustomerSiteTransformer());
    }

    public function includeSalesPerson(?SalesOrder $model): Item
    {
        return $this->item($model?->sales_person, new SalesPersonTransformer());
    }

    public function includePaymentTerm(?SalesOrder $model): Item
    {
        return $this->item($model?->payment_term, new PaymentTermTransformer());
    }

    public function includeDeliveryTerm(?SalesOrder $model): Item
    {
        return $this->item($model?->delivery_term, new DeliveryTermTransformer());
    }

    public function includeSalesPriceList(?SalesOrder $model): Item
    {
        return $this->item($model?->sales_price_list, new SalesPriceListTransformer());
    }

    public function includeWithholdingTax(?SalesOrder $model): Item
    {
        return $this->item($model?->withholding_tax, new TaxTransformer());
    }

    public function includeSite(?SalesOrder $model): Item
    {
        return $this->item($model?->site, new SiteTransformer());
    }

    public function includeDeliverySchedules(?SalesOrder $model): Collection
    {
        return $this->collection($model?->delivery_schedules, new SalesOrderDeliveryScheduleTransformer());
    }
}
