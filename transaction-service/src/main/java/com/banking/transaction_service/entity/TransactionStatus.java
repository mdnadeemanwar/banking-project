package com.banking.transaction_service.entity;

/**
 * Transaction life cycle flow
 * pending->process->completed (clean flow)
 *                   ->pending_verification(suspicious detected)
 *                       ->Completed(verified)
 *                       ->flagged (saga refund)
 *          ->failed
 *          ->flagged
 */
public enum TransactionStatus {
    PENDING,
    PROCESSING,
    PENDING_VERIFICATION,
    COMPLETED,
    FLAGGED,
    FAILED

}
