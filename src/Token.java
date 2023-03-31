//  ************** REQUIRES JAVA 17 OR ABOVE! (https://adoptium.net/) ************** //

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Remember this is part of a "fake" tokenizer, that when handed a string, it simply resolves to a
 * TOKEN object matching that string. All the Tokens/Terminals Used by the parser. The purpose of
 * the enum type here is to eliminate the need for direct character comparisons.
 * <p>
 * !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! IMPORTANT !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!<br>
 * -----------------------------------------------------------------------------<br>
 * IN *MOST* REAL CASES, THERE WILL BE ONLY ONE LEXEME PER compiler Token!
 * <p>
 * The fact that several lexemes exist per token in this example is because this is to parse simple
 * In English sentences, most of the token types have many words (lexemes) that could fit.
 * *** This is generally NOT the case in most programming languages!!! ***
 */
public enum Token {
    // Keywords
    READ("read"),
    WRITE("write"),
    WHILE("while"),
    DO("do"),
    OD("od"),
    IF("if"),
    THEN("then"),
    ELSE("else"),
    FI("fi"),
    UNTIL("until"),

    // Operators
    ASSIGN_OP(":="),
    ADD_OP("+", "-"),
    MULT_OP("*", "/"),
    LEFT_PAREN("("),
    RIGHT_PAREN(")"),
    REL_OP("<", ">", "<=", ">=", "=", "!="),

    // Identifiers and Numbers
    ID("sum" , "count", "n" , "abs"),
    NUMBER,

    // End of File
    EOF,

    // Unknown
    $$, // End of file
    // Unknown
    UNKNOWN;


    /**
     * A list of all lexemes for each token.
     */
    private final List<String> lexemeList;

    Token(final String... tokenStrings) {
        this.lexemeList = new ArrayList<>(tokenStrings.length);
        this.lexemeList.addAll(Arrays.asList(tokenStrings));
    }


    /**
     * Get a Token object from the Lexeme string.
     *
     * @param string The String (lexeme) to convert to a compiler.Token
     * @return A compiler.Token object based on the input String (lexeme)
     */
    public static Token fromLexeme(final String string) {
        // Just to be safe...
        final var lexeme = string.trim();

        // An empty string/lexeme should mean no more tokens to process.
        // Return the "end of input maker" if the string is empty.
        if (lexeme.isEmpty()) {
            return $$;
        }

        // Regex for one or more digits optionally followed by and more digits.
        // (doesn't handle "-", "+" etc., only digits)
        // Return the number token if the string represents a number.
        if (lexeme.matches("\\d+(?:\\.\\d+)?")) {
            return NUMBER;
        }

        // Search through ALL lexemes looking for a match with early bailout.
        // Return the matching token if found.
        for (var token : Token.values()) {
            if (token.lexemeList.contains(lexeme)) {
                // early bailout from for loop.
                return token;
            }
        }

        // NOTE: UNKNOWN could represent an ID, for example.
        // Return "UNKNOWN" if
        return UNKNOWN;
    }
}
