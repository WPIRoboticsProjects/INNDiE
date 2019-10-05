package edu.wpi.axon.tfdata.layer

import com.google.common.math.DoubleMath

sealed class Regularizer {

    /**
     * https://www.tensorflow.org/versions/r1.14/api_docs/python/tf/keras/regularizers/L1L2
     */
    data class L1L2(
        val l1: Double = 0.0,
        val l2: Double = 0.0
    ) : Regularizer() {

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is L1L2) return false

            if (!DoubleMath.fuzzyEquals(l1, other.l1, tolerance)) return false
            if (!DoubleMath.fuzzyEquals(l2, other.l2, tolerance)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = l1.hashCode()
            result = 31 * result + l2.hashCode()
            return result
        }

        companion object {
            private const val tolerance = 1e-9
        }
    }
}
