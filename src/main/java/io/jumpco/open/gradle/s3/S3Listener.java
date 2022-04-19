/*
    Copyright 2019-2021 Open JumpCO

    Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
    documentation files (the "Software"), to deal in the Software without restriction, including without limitation
    the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
    and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all copies or substantial
    portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
    THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF
    CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
    DEALINGS IN THE SOFTWARE.
 */
package io.jumpco.open.gradle.s3;

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
