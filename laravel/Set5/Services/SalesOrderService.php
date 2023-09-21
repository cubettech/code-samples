<?php

namespace App\Services\CustomersAndSales;

use App\Exports\ModelDetailExports\CustomersAndSales\SalesOrderModelDetailExport;
use App\Models\CustomersAndSales\SalesOrder\SalesOrder;
use App\Services\BaseService;
use App\Support\SalesOrderLineItemCollection;
use App\Transformers\CustomersAndSales\SalesOrder\SalesOrderTransformer;
use Illuminate\Support\Arr;

class SalesOrderService extends BaseService
{
    protected string $transformer = SalesOrderTransformer::class;
    protected string $model = SalesOrder::class;
    protected ?string $frontendIndexUrl = "/customers-and-sales/sales-orders";
    protected ?string $frontendCreateUrl = "/customers-and-sales/sales-orders/create";
    protected ?string $frontendEditUrl = "/customers-and-sales/sales-orders/{id}/edit";
    protected ?string $modelDetailExportClass = SalesOrderModelDetailExport::class;

    protected array $itemWith = [
        'lines',
        'lines.item',
        'lines.tax_group',
        'lines.tax_group.taxes',
        'total_taxes',
        'total_taxes.tax',
        'lines.item_uom',
        'lines.item_uom.unit_of_measurement',
        'customer',
        'customer_site',
        'customer_site.billing_address',
        'customer_site.shipping_address',
        'sales_person',
        'sales_person.employee',
        'sales_price_list',
        'payment_term',
        'delivery_term',
        'withholding_tax',
        'sales_price_list',
        'site',
    ];

    protected array $itemIncludes = [
        'lines',
        'lines.item',
        'lines.tax_group',
        'lines.tax_group.taxes',
        'total_taxes',
        'total_taxes.tax',
        'lines.item_uom',
        'lines.item_uom.unit_of_measurement',
        'customer',
        'customer_site',
        'customer_site.billing_address',
        'customer_site.shipping_address',
        'sales_person',
        'sales_person.employee',
        'sales_price_list',
        'payment_term',
        'delivery_term',
        'sales_price_list',
        'site',
    ];

    protected array $collectionIncludes = [
        'customer',
        'customer_site',
    ];

    protected array $orderMap = [
        'id' => 'id',
        'named_id' => 'named_id',
        'order_number' => 'named_id',
        'status' => 'status',
    ];

    public function handleSave($model, $data)
    {
        $lines = collect(Arr::get($data, 'lines.data'))
            ->filter(fn($l) => !empty(Arr::get($l, 'item_id')));

        $linesCollection = new SalesOrderLineItemCollection(
            items: $lines,
            currency: Arr::get($data, 'currency_id', getBaseCurrency()),
            exchange_rate: Arr::get($data, 'exchange_rate', 1),
            withholding_tax: null,
        );

        $model->fill([
            'named_id' => $model?->named_id ?? getNextSequence('sales_order'),

            'due_date' => null,
            'order_date' => Arr::get($data, 'order_date'),
            'requested_delivery_date' => Arr::get($data, 'requested_delivery_date'),

            'purchase_order_date' => Arr::get($data, 'purchase_order_date'),
            'purchase_order_number' => Arr::get($data, 'purchase_order_number'),

            'sales_person_id' => Arr::get($data, 'sales_person_id'),
            'payment_term_id' => Arr::get($data, 'payment_term_id'),
            'delivery_term_id' => Arr::get($data, 'delivery_term_id'),
            'customer_id' => Arr::get($data, 'customer_id'),
            'customer_site_id' => Arr::get($data, 'customer_site_id'),
            'sales_price_list_id' => Arr::get($data, 'sales_price_list_id'),
            'site_id' => Arr::get($data, 'site_id'),
            'description' => Arr::get($data, 'description'),

            'status' => $model?->status ?? SalesOrder::STATUS_OPEN,
            'phase' => $model?->phase ?? SalesOrder::PHASE_DRAFT,
            'memo' => Arr::get($data, 'memo'),
            'internal_note' => Arr::get($data, 'internal_note'),

            'promised_delivery_date' => Arr::get($data, 'promised_delivery_date'),
        ]);

        $model->save();

        $linesCollection->save($model);

        $model->load($this->itemWith);

        return $model;
    }

    protected function handleSearch($query, $search)
    {
        return $query->orWhere("id", "ILIKE", "%$search%")
            ->orWhere("internal_note", "ILIKE", "%$search%")
            ->orWhere("named_id", "ILIKE", "%$search%")
            ->orWhere("purchase_order_number", "ILIKE", "%$search%")
            ->orWhere("description", "ILIKE", "%$search%")
            ->orWhere("memo", "ILIKE", "%$search%");
    }

    public function afterDestroy($model): bool
    {
        parent::afterDestroy($model);

        $model->lines()->delete();

        return true;
    }
}
