package io.github.streamingpipeline.rowkey;

import io.github.streamingpipeline.exception.AccountsPipelineException;
import io.github.streamingpipeline.model.UberAccount;
import io.github.streamingpipeline.model.Institution;
import io.github.streamingpipeline.utils.ErrorCode;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
	public String buildAccountLookupRowKey(UberAccount account, Institution institution) {
        return account.getInstitutionId() + "#ai#";
    }
}