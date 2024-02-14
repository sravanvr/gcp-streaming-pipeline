package io.github.streamingpipeline.rowkey;

import io.github.streamingpipeline.model.UberAccount;
import io.github.streamingpipeline.model.UberCustomer;

/***
 *
 * @author Sravan Vedala
 *
 */
public class UberAccountLookupRowKey {

    /**
     * Implement row-key formulation logic here as desired.
     * @param account
     * @param institution
     * @return
     */
	public String buildAccountLookupRowKey(UberAccount account, UberCustomer institution) {
        return account.getUberCustomerId() + "#ai#";
    }
}