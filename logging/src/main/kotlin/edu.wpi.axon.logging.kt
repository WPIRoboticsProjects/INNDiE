/**
 * Calls [joinToString] with an indent applied to each separated line.
 *
 * @param indent The indent for each line, typically a tab character.
 * @return The string.
 */
fun <T> Iterable<T>.joinWithIndent(indent: String) =
    joinToString(separator = "\n$indent", prefix = indent)
