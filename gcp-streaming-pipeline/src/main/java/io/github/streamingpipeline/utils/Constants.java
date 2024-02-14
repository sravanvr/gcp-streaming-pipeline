/*
 * Copyright (C) 2020 NCR Corporation. This material contains certain trade
 * secrets and confidential and proprietary information of NCR Corporation.
 * Use, reproduction, disclosure and distribution by any means are prohibited,
 * except pursuant to a written license from NCR Corporation. Use of copyright
 * notice is precautionary and does not imply publication or disclosure.
 */

package io.github.streamingpipeline.utils;

public class Constants {
	public static final String UTF8 = "UTF-8";	
    public static final String COLUMN_FAMILY_KEY_COLUMNS = "key_columns";
    public static final String COLUMN_FAMILY = "data";    
    public static final String ACCOUNT_COLUMN_QUALIFIER = "record";    
    public static final String TAG_COLUMN_QUALIFIER = "tags";
    public static final String LOOKUP_COLUMN_FAMILY = "data";
    public static final String LOOKUP_COLUMN_QUALIFIER = "lookup";
        
    //TupleTags constants
    public static final String ACCOUNT_TAG="ACCOUNT_TAG";
    public static final String ACCOUNT_LOOKUP="ACCOUNT_LOOKUP";
    public static final String CUSTOMER_ACCOUNT_LOOKUP="CUSTOMER_ACCOUNT_LOOKUP";
    
    // Tags
    public static final String IMPORT_RECORD_LINE_NUMBER = "importRecordLineNumber";
    public static final String IMPORT_ID = "importId";
    public static final String FILE_NAME = "fileName";
    public static final String FILE_POSTING_DATE = "filePostingDate";
    public static final String BLOCK_NUMBER = "block";
    public static final String MESSAGE_SOURCE = "source";
    public static final String FILE_LOG_ID = "fileLogId";
    public static final String BATCH_LOG_ID = "batchLogId";
}
