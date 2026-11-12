package vn.hvktmm.bach.securitylab.logic

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TransferPolicyTest {
    @Test fun regularUserIsLimitedToTwoMillion() {
        assertTrue(TransferPolicy.canTransfer(2_000_000, false, false))
        assertFalse(TransferPolicy.canTransfer(2_000_001, false, false))
    }
    @Test fun adminHasHighestLimit() {
        assertTrue(TransferPolicy.canTransfer(50_000_000, true, false))
        assertFalse(TransferPolicy.canTransfer(50_000_001, true, true))
    }
}
