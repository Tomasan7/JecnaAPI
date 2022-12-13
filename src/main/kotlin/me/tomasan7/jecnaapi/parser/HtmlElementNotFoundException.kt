package me.tomasan7.jecnaapi.parser

class HtmlElementNotFoundException private constructor(message: String) : ParseException(message)
{
    companion object
    {
        fun bySelector(selector: String) = HtmlElementNotFoundException("Element with selector '$selector' not found.")

        fun byName(name: String) = HtmlElementNotFoundException("Element '$name' not found.")
    }
}