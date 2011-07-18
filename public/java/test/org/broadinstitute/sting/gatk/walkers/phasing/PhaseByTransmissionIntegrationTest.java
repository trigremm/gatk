package org.broadinstitute.sting.gatk.walkers.phasing;

import org.broadinstitute.sting.WalkerTest;
import org.testng.annotations.Test;

import java.util.Arrays;

public class PhaseByTransmissionIntegrationTest extends WalkerTest {
    private static String phaseByTransmissionTestDataRoot = validationDataLocation + "/PhaseByTransmission";
    private static String fundamentalTestVCF = phaseByTransmissionTestDataRoot + "/" + "FundamentalsTest.unfiltered.vcf";

    @Test
    public void testBasicFunctionalityWithoutFilters() {
        WalkerTestSpec spec = new WalkerTestSpec(
                buildCommandLine(
                        "-T PhaseByTransmission",
                        "-R " + b37KGReference,
                        "-B:variant,VCF " + fundamentalTestVCF,
                        "-f NA12892+NA12891=NA12878",
                        "-nofilters",
                        "-o %s"
                ),
                1,
                Arrays.asList("")
        );
        executeTest("testBasicFunctionalityWithoutFilters", spec);
    }

    @Test
    public void testBasicFunctionalityWithFilters() {
        WalkerTestSpec spec = new WalkerTestSpec(
                buildCommandLine(
                        "-T PhaseByTransmission",
                        "-R " + b37KGReference,
                        "-B:variant,VCF " + fundamentalTestVCF,
                        "-f NA12892+NA12891=NA12878",
                        "-o %s"
                ),
                1,
                Arrays.asList("")
        );
        executeTest("testBasicFunctionalityWithFilters", spec);
    }
}
