package no.tomlin.api.common

class AffectedIncorrectNumberOfRowsException(actual: Int) :
    RuntimeException("SQL update affected $actual rows, not 1 or 2 as expected")
