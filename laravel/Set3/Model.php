<?php

namespace App\Models\Banking;

use App\Abstracts\Model;
use App\Traits\Transactions;
use Illuminate\Database\Eloquent\Factories\HasFactory;

class Account extends Model
{
    use Cloneable, HasFactory, Transactions;

    protected $table = 'accounts';

    /**
     * The accessors to append to the model's array form.
     *
     * @var array
     */
    protected $appends = ['balance', 'without_tax_balance','converted_amount','converted_expense','converted_income'];

    /**
     * Attributes that should be mass-assignable.
     *
     * @var array
     */
    protected $fillable = ['company_id', 'name', 'number', 'currency_code', 'opening_balance', 'bank_name', 'bank_phone', 'bank_address', 'enabled', 'created_from', 'created_by'];

    /**
     * The attributes that should be cast.
     *
     * @var array
     */
    protected $casts = [
        'opening_balance' => 'double',
        'enabled' => 'boolean',
    ];

    /**
     * Sortable columns.
     *
     * @var array
     */
    public $sortable = ['name', 'number', 'opening_balance', 'enabled'];

    public function currency()
    {
        return $this->belongsTo('App\Models\Setting\Currency', 'currency_code', 'code');
    }

    public function scopeName($query, $name)
    {
        return $query->where('name', '=', $name);
    }

    public function scopeNumber($query, $number)
    {
        return $query->where('number', '=', $number);
    }

    /**
     * Get the current balance.
     *
     * @return string
     */
    public function getBalanceAttribute()
    {
        // Opening Balance
        $total = $this->opening_balance;

        // Sum Incomes
        $total += $this->income_transactions->sum('amount');

        // Subtract Expenses
        $total -= $this->expense_transactions->sum('amount');

        return $total;
    }

    /**
     * Get the current balance without tax.
     *
     * @return string
     */
    public function getWithoutTaxBalanceAttribute()
    {
        // Opening Balance
        $total = $this->opening_balance;

        // Sum Incomes
        $total += ( $this->income_transactions->sum('amount') - $this->income_transactions->sum('tax') );
        // Subtract Expenses
        $total -= ( $this->expense_transactions->sum('amount') - $this->expense_transactions->sum('tax') );
        return $total;
    }

    public function getConvertedAmountAttribute(){
            // Opening Balance
            $total = $this->opening_balance;
            // Sum Incomes
            // $total += ( $this->income_transactions->sum('amount') - $this->income_transactions->sum('tax') );
            foreach($this->income_transactions as $transaction){
                $total += $transaction->getAmountConvertedToDefault(false,setting('default.tax_profit')?true:false);
            }
            // Subtract Expenses
            // $total -= ( $this->expense_transactions->sum('amount') - $this->expense_transactions->sum('tax') );
            foreach($this->expense_transactions as $transaction){
                $total -= $transaction->getAmountConvertedToDefault(false,setting('default.tax_profit')?true:false);
            }
            return $total;
    }
    /**
     * Get the current balance.
     *
     * @return string
     */
    public function getIncomeBalanceAttribute()
    {
        // Opening Balance
        //$total = $this->opening_balance;
        $total = 0;

        // Sum Incomes
        $total += $this->income_transactions->sum('amount');

        return $total;
    }

    public function getConvertedIncomeAttribute(){
        $total = 0;
          // Subtract Expenses
        //   $total += $this->expense_transactions->sum('amount');
        foreach($this->income_transactions as $transaction){
            $total += $transaction->getAmountConvertedToDefault(false,setting('default.tax_profit')?true:false);
        }
        return $total;
    }
    /**
     * Get the current balance.
     *
     * @return string
     */
    public function getExpenseBalanceAttribute()
    {
        // Opening Balance
        //$total = $this->opening_balance;
        $total = 0;

        // Subtract Expenses
        $total += $this->expense_transactions->sum('amount');

        return $total;
    }

    public function getConvertedExpenseAttribute(){
        $total = 0;
         // Subtract Expenses
        //   $total += $this->expense_transactions->sum('amount');
        foreach($this->expense_transactions as $transaction){
            $total += $transaction->getAmountConvertedToDefault(false,setting('default.tax_profit')?true:false);
        }
        return $total;
    }
    /**
     * Create a new factory instance for the model.
     *
     * @return \Illuminate\Database\Eloquent\Factories\Factory
     */
    protected static function newFactory()
    {
        return \Database\Factories\Account::new();
    }

}
