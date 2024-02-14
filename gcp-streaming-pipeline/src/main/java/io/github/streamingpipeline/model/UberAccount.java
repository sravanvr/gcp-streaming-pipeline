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
    private String institutionId;
    private String fileLogId;
    private String batchLogId;
    private SeverityLevel severityLevel;
    private String errorLogId;
    private String errorMessage;
    private String recordNum;
}

