/*
 * Copyright 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.infosmid.gradle.s3;

import com.amazonaws.event.ProgressEvent;
import com.amazonaws.event.ProgressListener;
import com.amazonaws.services.s3.transfer.Transfer;
import org.gradle.api.logging.Logger;
import java.text.DecimalFormat;

public class S3Listener implements ProgressListener {

    private DecimalFormat df = new DecimalFormat("#0.0");

    private Transfer transfer;

    private Logger logger;

    public S3Listener(Transfer transfer, Logger logger) {
        this.transfer = transfer;
        this.logger = logger;
    }

    public void progressChanged(ProgressEvent e) {
        switch (e.getEventType()) {
            case TRANSFER_COMPLETED_EVENT:
            case TRANSFER_STARTED_EVENT:
                logger.lifecycle(String.format("%s%s", transfer.getState().name(), transfer.getDescription()));
            default:
                logger.info(String.format("%s%%", df.format(transfer.getProgress().getPercentTransferred())));
        }
    }
}
