package org.broadinstitute.sting.gatk.walkers.reducereads;

import com.google.java.contract.Ensures;
import com.google.java.contract.Requires;
import net.sf.samtools.SAMRecord;
import org.broadinstitute.sting.utils.sam.AlignmentStartWithNoTiesComparator;

import java.util.Comparator;

public class SlidingReadComparator implements Comparator<SlidingRead> {
    @Requires("slidingRead1 != null && slidingRead2 != null")
    @Ensures("result == 0 || result == 1 || result == -1")
    public int compare(SlidingRead slidingRead1, SlidingRead slidingRead2) {
        SAMRecord r1 = slidingRead1.getRead();
        SAMRecord r2 = slidingRead2.getRead();

        AlignmentStartWithNoTiesComparator comp = new AlignmentStartWithNoTiesComparator();

        return comp.compare(r1, r2);

//        if (r1 == r2)
//            result = 0;
//
//        else if (r1.getReadUnmappedFlag())
//            result = 1;
//        else if (r2.getReadUnmappedFlag())
//            result = -1;
//        else {
//            final int cmpContig = compareContigs(r1.getReferenceIndex(), r2.getReferenceIndex());
//
//            if (cmpContig != 0)
//                result = cmpContig;
//
//            else {
//                if (r1.getAlignmentStart() < r2.getAlignmentStart()) result = -1;
//                else result = 1;
//            }
//        }
//
//        return result;
    }
}