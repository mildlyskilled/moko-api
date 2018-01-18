trait InputValidator[T] {
    def validate(T): Boolean
    def message: String
}

object InputValidator {
    case object StringValidator extends InputValidator[String]{
        def validate(input: String): Boolean = 
    }
}