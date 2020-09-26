package no.tomlin.api.common

class AffectedIncorrectNumberOfRowsException(actual: Int, min: Int = 1, max: Int = 2) :
    RuntimeException("SQL update affected $actual rows, not in range [$min, $max]")
