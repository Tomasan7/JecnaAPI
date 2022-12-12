package me.tomasan7.jecnaapi.parser

class HtmlElementNotFoundException(elementSelector: String) : ParseException("No match for selector '$elementSelector' found.")