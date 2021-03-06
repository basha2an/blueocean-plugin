package io.blueocean.ath.offline.pipeline;

import com.google.inject.Inject;
import io.blueocean.ath.ATHJUnitRunner;
import io.blueocean.ath.BlueOceanAcceptanceTest;
import io.blueocean.ath.WebDriverMixin;
import io.blueocean.ath.factory.ClassicPipelineFactory;
import io.blueocean.ath.factory.FreestyleJobFactory;
import io.blueocean.ath.model.ClassicPipeline;
import io.blueocean.ath.sse.SSEClientRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(ATHJUnitRunner.class)
public class DownstreamLinkTest extends BlueOceanAcceptanceTest implements WebDriverMixin {

    @Inject
    ClassicPipelineFactory pipelineFactory;

    @Inject
    FreestyleJobFactory freestyleJobFactory;

    @Inject
    @Rule
    public SSEClientRule sseClient;

    @Test
    public void downstreamJobLinkAppearsInRunResult() throws Exception {

        final String upstreamJobScript = "stage ('stageName') { build 'downstreamJob' }";
        ClassicPipeline upstreamJob = pipelineFactory.pipeline("upstreamJob").createPipeline(upstreamJobScript);

        freestyleJobFactory.pipeline("downstreamJob").create("echo blah");

        upstreamJob.build();
        sseClient.untilEvents(upstreamJob.buildsFinished);
        sseClient.clear();

        upstreamJob.getRunDetailsPipelinePage().open(1);

        find("//*[contains(text(),'Triggered Builds')]").isVisible(); // Heading for table of builds
        find("//*[contains(text(),'downstreamJob')]").isVisible(); // row pointing to downstream build
    }
}
