package de.fau.i1.aka.avt.Accessibility;

/**
 * Created by Anatoli Kalysch (anatoli.kalysch@fau.de), Department of Computer Science, Friedrich-Alexander University Erlangen-Nuremberg, on 14.11.17.
 */

public enum TransactionState {
    INIT,
    LOGGED_IN,
    START_TRANSACTION,
    TRANSACTION_FINISHING_TOUCH,
    END_TRANSACTION,
    FINISHED
}
