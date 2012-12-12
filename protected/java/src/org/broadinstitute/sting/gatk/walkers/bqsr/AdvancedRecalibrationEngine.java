package org.broadinstitute.sting.gatk.walkers.bqsr;

/*
 * Copyright (c) 2009 The Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

import org.broadinstitute.sting.utils.classloader.ProtectedPackageSource;
import org.broadinstitute.sting.utils.recalibration.EventType;
import org.broadinstitute.sting.utils.recalibration.ReadCovariates;
import org.broadinstitute.sting.utils.recalibration.RecalibrationTables;
import org.broadinstitute.sting.utils.recalibration.covariates.Covariate;
import org.broadinstitute.sting.utils.sam.GATKSAMRecord;
import org.broadinstitute.sting.utils.threading.ThreadLocalArray;

public class AdvancedRecalibrationEngine extends StandardRecalibrationEngine implements ProtectedPackageSource {

    // optimization: only allocate temp arrays once per thread
    private final ThreadLocal<byte[]> threadLocalTempQualArray = new ThreadLocalArray<byte[]>(EventType.values().length, byte.class);
    private final ThreadLocal<double[]> threadLocalTempFractionalErrorArray = new ThreadLocalArray<double[]>(EventType.values().length, double.class);

    public void initialize(final Covariate[] covariates, final RecalibrationTables recalibrationTables) {
        super.initialize(covariates, recalibrationTables);
    }

    @Override
    public void updateDataForRead(final GATKSAMRecord read, final boolean[] skip, final double[] snpErrors, final double[] insertionErrors, final double[] deletionErrors ) {
        for( int offset = 0; offset < read.getReadBases().length; offset++ ) {
            if( !skip[offset] ) {
                final ReadCovariates readCovariates = covariateKeySetFrom(read);

                byte[] tempQualArray = threadLocalTempQualArray.get();
                double[] tempFractionalErrorArray = threadLocalTempFractionalErrorArray.get();

                tempQualArray[EventType.BASE_SUBSTITUTION.index] = read.getBaseQualities()[offset];
                tempFractionalErrorArray[EventType.BASE_SUBSTITUTION.index] = snpErrors[offset];
                tempQualArray[EventType.BASE_INSERTION.index] = read.getBaseInsertionQualities()[offset];
                tempFractionalErrorArray[EventType.BASE_INSERTION.index] = insertionErrors[offset];
                tempQualArray[EventType.BASE_DELETION.index] = read.getBaseDeletionQualities()[offset];
                tempFractionalErrorArray[EventType.BASE_DELETION.index] = deletionErrors[offset];

                for (final EventType eventType : EventType.values()) {
                    final int[] keys = readCovariates.getKeySet(offset, eventType);
                    final int eventIndex = eventType.index;
                    final byte qual = tempQualArray[eventIndex];
                    final double isError = tempFractionalErrorArray[eventIndex];

                    incrementDatumOrPutIfNecessary(recalibrationTables.getQualityScoreTable(), qual, isError, keys[0], keys[1], eventIndex);

                    for (int i = 2; i < covariates.length; i++) {
                        if (keys[i] < 0)
                            continue;

                        incrementDatumOrPutIfNecessary(recalibrationTables.getTable(i), qual, isError, keys[0], keys[1], keys[i], eventIndex);
                    }
                }
            }
        }
    }
}
