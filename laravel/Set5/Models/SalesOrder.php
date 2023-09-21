<?php

namespace App\Models\CustomersAndSales\SalesOrder;

use App\Concerns\HasModelRecordHistory;
use App\Models\BaseModel;
use App\Models\CustomersAndSales\Customer\Customer;
use App\Models\CustomersAndSales\DeliveryTerm;
use App\Models\CustomersAndSales\Invoice\Invoice;
use App\Models\CustomersAndSales\PaymentTerm;
use App\Models\CustomersAndSales\SalesPerson;
use App\Models\CustomersAndSales\SalesPriceList\SalesPriceList;
use App\Models\InventoryManagement\Site;
use App\Models\Tax;
use Illuminate\Database\Eloquent\Factories\HasFactory;
use Illuminate\Database\Eloquent\Relations\BelongsTo;
use Illuminate\Database\Eloquent\Relations\HasMany;
use Illuminate\Database\Eloquent\Relations\HasOne;

class SalesOrder extends BaseModel
{
    use HasFactory;
    use HasModelRecordHistory;

    public const STATUS_DRAFT = 'Draft';
    public const STATUS_OPEN = 'Open';
    public const STATUS_CLOSED = 'Closed';

    public const PHASE_DRAFT = 'Authorized';

    protected $table = 'fi_sales_orders';

    protected $fillable = [
        'is_opening_balance',
        'customer_id',
        'sales_contract_id',
        'sales_person_id',
        'customer_site_id',
        'sales_price_list_id',
        'payment_term_id',
        'delivery_term_id',
        'withholding_tax_id',
        'site_id',

        'named_id',
        'purchase_order_number',
        'status',
        'phase',
        'memo',
        'internal_note',

        'currency_id',
        'exchange_rate',
        'gross_amount',
        'gross_amount_bc',
        'discount_amount',
        'discount_amount_bc',
        'net_amount',
        'net_amount_bc',
        'tax_amount',
        'tax_amount_bc',
        'total_amount',
        'total_amount_bc',
        'withholding_tax_id',
        'withholding_tax_amount',
        'withholding_tax_amount_bc',
        'grand_total_amount',
        'grand_total_amount_bc',
        'description',

        'order_date',
        'purchase_order_date',
        'requested_delivery_date',
        'due_date',
        'completed_at',
        'posted_at',
        'promised_delivery_date',
    ];

    protected $dates = [
        'order_date',
        'purchase_order_date',
        'requested_delivery_date',
        'completed_at',
        'posted_at',
        'promised_delivery_date',
    ];

    public function invoice(): HasOne
    {
        return $this->hasOne(Invoice::class, 'sales_order_id', 'id');
    }

    public function invoices(): HasMany
    {
        return $this->hasMany(Invoice::class, 'sales_order_id', 'id');
    }

    public function lines(): HasMany
    {
        return $this->hasMany(SalesOrderLine::class, 'sales_order_id', 'id')
            ->orderBy('line', 'asc');
    }

    public function total_taxes(): HasMany
    {
        return $this->hasMany(SalesOrderTotalTax::class, 'sales_order_id', 'id');
    }

    public function customer(): BelongsTo
    {
        return $this->belongsTo(Customer::class, 'customer_id', 'id');
    }

    public function customer_site(): BelongsTo
    {
        return $this->belongsTo(Site::class, 'customer_site_id');
    }

    public function sales_person(): BelongsTo
    {
        return $this->belongsTo(SalesPerson::class, 'sales_person_id', 'id');
    }

    public function sales_price_list(): BelongsTo
    {
        return $this->belongsTo(SalesPriceList::class, 'sales_price_list_id', 'id');
    }

    public function payment_term(): BelongsTo
    {
        return $this->belongsTo(PaymentTerm::class, 'payment_term_id');
    }

    public function delivery_term(): BelongsTo
    {
        return $this->belongsTo(DeliveryTerm::class, 'delivery_term_id');
    }

    public function withholding_tax(): BelongsTo
    {
        return $this->belongsTo(Tax::class, 'withholding_tax_id');
    }

    public function site(): BelongsTo
    {
        return $this->belongsTo(Site::class, 'site_id', 'id');
    }

    public function close(): void
    {
        if (!$this->closed_at) {
            $this->closed_at = now();

            static::save();
        }
    }

    public function scopeOpenOrders($query): void
    {
        $query->where('status', self::STATUS_OPEN);
    }

    public function scopeClosedOrders($query): void
    {
        $query->where('status', self::STATUS_CLOSED);
    }

    public function delivery_schedules(): HasMany
    {
        return $this->hasMany(SalesOrderDeliverySchedule::class, 'sales_order_id', 'id');
    }
}
