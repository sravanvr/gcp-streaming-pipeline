package io.github.streamingpipeline.model;

import io.github.streamingpipeline.utils.SeverityLevel;
import lombok.*;

/***
 *
 * @author SravanVedala
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UberAccount {
    private String Id;
    private String accountId;
    private String uberCustomerId;
    private String uberFileLogId;
    private String uberBatchLogId;
    private SeverityLevel severityLevel;
    private String uberErrorLogId;
    private String errorMessage;
    private String recordNum;
}

