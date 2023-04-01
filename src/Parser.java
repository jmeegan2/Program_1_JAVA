//  ************** REQUIRES JAVA 17 OR ABOVE! (https://adoptium.net/) ************** //

import java.util.logging.Logger;

/*
 * GRAMMAR FOR PROCESSING SIMPLE SENTENCES:
 *
 * <SENTENCE> ::= <NOUN_PHRASE> <VERB_PHRASE> <NOUN_PHRASE> <PREP_PHRASE> <SENTENCE_TAIL> $$
 * <SENTENCE_TAIL> ::= <CONJ> <SENTENCE> | <EOS>
 *
 * <NOUN_PHRASE> ::= <ART> <ADJ_LIST> <NOUN>
 * <ADJ_LIST> ::= <ADJECTIVE> <ADJ_TAIL> | <<EMPTY>>
 * <ADJ_TAIL> ::= <COMMA> <ADJECTIVE> <ADJ_TAIL> | <<EMPTY>>
 *
 * <VERB_PHRASE> ::= <ADVERB> <VERB> | <VERB>
 * <PREP_PHRASE> ::= <PREPOSITION> <NOUN_PHRASE> | <<EMPTY>>
 *
 * // *** Terminal Productions (Actual terminals omitted, but they are just the
 * valid words in the language). ***
 *
 * <COMMA> ::= ','
 * <EOS> ::= '.' | '!'
 *
 * <ADJECTIVE> ::= ...adjective list...
 * <ADVERB> ::= ...adverb list...
 * <ART> ::= ...article list...
 * <CONJ> ::= ...conjunction list...
 * <NOUN> ::= ...noun list...
 * <PREPOSITION> ::= ...preposition list...
 * <VERB> ::= ...verb list....
 */

/**
 * The Syntax Analyzer.
 * <p>
 * ************** NOTE: REQUIRES JAVA 11 OR ABOVE! ******************
 */
public class Parser {

    // The lexer which will provide the tokens
    private final LexicalAnalyzer lexer;

    // The actual "code generator"
    private final CodeGenerator codeGenerator;

    /**
     * This is the constructor for the Parser class which
     * accepts a LexicalAnalyzer and a CodeGenerator object as parameters.
     *
     * @param lexer         The Lexer Object
     * @param codeGenerator The CodeGenerator Object
     */
    public Parser(LexicalAnalyzer lexer, CodeGenerator codeGenerator) {
        this.lexer = lexer;
        this.codeGenerator = codeGenerator;

        // Change this to automatically prompt to see the Open WebGraphViz dialog or not.
        MAIN.PROMPT_FOR_GRAPHVIZ = true;
    }

    /*
     * Since the "Compiler" portion of the code knows nothing about the start rule,
     * the "analyze" method must invoke the start rule.
     *
     * Begin analyzing...
     */
    void analyze() {
        try {
            // Generate header for our output
            TreeNode startNode = codeGenerator.writeHeader("PARSE TREE");

            // THIS IS OUR START RULE
            this.beginParsing(startNode);

            // generate footer for our output
            codeGenerator.writeFooter();

        } catch (ParseException ex) {
            final String msg = String.format("%s\n", ex.getMessage());
            Logger.getAnonymousLogger().severe(msg);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * This is just an intermediate method to make it easy to change the start rule of the grammar.
     *
     * @param parentNode The parent node for the parse tree
     * @throws ParseException If there is a syntax error
     */
    private void beginParsing(final TreeNode parentNode) throws ParseException {
        // Invoke the start rule.
        // TODO: Change if necessary!
        //we should change this to pertain to our code
        this.Program(parentNode);
    }

    // Start rule
    private void Program(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode, "Program");
        StmtList(thisNode);
        MATCH(thisNode, Token.EOF); // Ensure that we have parsed the entire input.
    }

    private void StmtList(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode, "StmtList");
        if (lexer.currentToken() == Token.ID || lexer.currentToken() == Token.READ || lexer.currentToken() == Token.WRITE || lexer.currentToken() == Token.IF || lexer.currentToken() == Token.WHILE) {
            stmt(thisNode);
            StmtList(thisNode);
        } else {
            EMPTY(thisNode);
        }
    }

    private void stmt(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode, "<stmt>");

        switch (lexer.currentToken()) {
            case ID:
                MATCH(thisNode, Token.ID);
                MATCH(thisNode, Token.ASSIGN_OP);
                Expr(thisNode);
                break;
            case READ:
                MATCH(thisNode, Token.READ);
                MATCH(thisNode, Token.ID);
                break;
            case WRITE:
                MATCH(thisNode, Token.WRITE);
                Expr(thisNode);
                break;
            case IF:
                if_stmt(thisNode);
                break;
            case WHILE:
                while_stmt(thisNode);
                break;
            case DO: // Add new case for the "do" keyword
                do_until_stmt(thisNode);
                break;
            case EOF:
                // Handle end of file token
                break;

            default:
                EMPTY(thisNode);
        }
    }

    private void do_until_stmt(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode, "<do_until_stmt>");

        MATCH(thisNode, Token.DO);
        StmtList(thisNode);
        MATCH(thisNode, Token.UNTIL);
        condition(thisNode);
    }

    private void if_stmt(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode, "<if_stmt>");

        MATCH(thisNode, Token.IF);
        condition(thisNode);
        MATCH(thisNode, Token.THEN);
        StmtList(thisNode);
        else_part(thisNode);
        if (lexer.currentToken() == Token.FI) {
            MATCH(thisNode, Token.FI);
        } else if (lexer.currentToken() == Token.IF) {
            if_stmt(thisNode);
        } else {
            raiseException(Token.FI, thisNode);
        }
    }


    private void else_part(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode, "<else_part>");

        if (lexer.currentToken() == Token.ELSE) {
            MATCH(thisNode, Token.ELSE);
            StmtList(thisNode);
        } else if (lexer.currentToken() == Token.FI || lexer.currentToken() == Token.EOF) {
            EMPTY(thisNode);
        } else {
            stmt(thisNode);
        }
    }


    private void while_stmt(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode, "<while_stmt>");

        MATCH(thisNode, Token.WHILE);
        condition(thisNode);
        MATCH(thisNode, Token.DO);
        StmtList(thisNode);
        MATCH(thisNode, Token.OD);
    }

    private void condition(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode, "<condition>");

        Expr(thisNode);
        MATCH(thisNode, Token.REL_OP);
        Expr(thisNode);
    }


    private void Expr(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode, "Expr");
        Expo(thisNode);
    }

    private void Expo(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode, "Expo");
        Term(thisNode);
        TermTail(thisNode);
    }

    private void TermTail(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode, "TermTail");
        if (lexer.currentToken() == Token.ADD_OP) {
            MATCH(thisNode, Token.ADD_OP);
            Term(thisNode);
            TermTail(thisNode);
        } else {
            EMPTY(thisNode);
        }
    }

    private void Term(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode, "Term");
        Factor(thisNode);
        FactorTail(thisNode);
    }

    private void FactorTail(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode, "FactorTail");
        if (lexer.currentToken() == Token.MULT_OP) {
            MATCH(thisNode, Token.MULT_OP);
            Factor(thisNode);
            FactorTail(thisNode);
        } else {
            EMPTY(thisNode);
        }
    }

    private void Factor(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode, "Factor");
        if (lexer.currentToken() == Token.LEFT_PAREN) {
            MATCH(thisNode, Token.LEFT_PAREN);
            Expr(thisNode);
            MATCH(thisNode, Token.RIGHT_PAREN);
        } else if (lexer.currentToken() == Token.ID) {
            MATCH(thisNode, Token.ID);
        } else if (lexer.currentToken() == Token.NUMBER) {
            MATCH(thisNode, Token.NUMBER);
        } else {
            raiseException(Token.LEFT_PAREN, thisNode);
        }
    }




    /////////////////////////////////////////////////////////////////////////////////////

    /**
     * Add a an EMPTY terminal node (result of an Epsilon Production) to the parse tree.
     * Mainly, this is just done for better visualizing the complete parse tree.
     *
     * @param parentNode The parent of the terminal node.
     */
    void EMPTY(final TreeNode parentNode) {
        codeGenerator.addEmptyToTree(parentNode);
    }

    /**
     * Match the current token with the expected token.
     * If they match, add the token to the parse tree, otherwise throw an exception.
     *
     * @param parentNode    The parent of the terminal node.
     * @param expectedToken The token to be matched.
     * @throws ParseException Thrown if the token does not match the expected token.
     */
    void MATCH(final TreeNode parentNode, final Token expectedToken) throws ParseException {
        final Token currentToken = lexer.currentToken();

        if (currentToken == expectedToken) {
            var currentLexeme = lexer.getCurrentLexeme();
            this.addTerminalToTree(parentNode, currentToken, currentLexeme);
            lexer.advanceToken();
        } else {
            this.raiseException(expectedToken, parentNode);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Add a terminal node to the parse tree.
     *
     * @param parentNode    The parent of the terminal node.
     * @param currentToken  The token to be added.
     * @param currentLexeme The lexeme of the token beign added.
     * @throws ParseException Throws a ParseException if the token cannot be added to the tree.
     */
    void addTerminalToTree(final TreeNode parentNode, final Token currentToken, final String currentLexeme) throws ParseException {
        var nodeLabel = "<%s>".formatted(currentToken);
        var terminalNode = codeGenerator.addNonTerminalToTree(parentNode, nodeLabel);

        codeGenerator.addTerminalToTree(terminalNode, currentLexeme);
    }

    /**
     * Raise a ParseException if the input cannot be parsed as defined by the grammar.
     *
     * @param expected   The expected token
     * @param parentNode The token's parent node
     */
    private void raiseException(Token expected, TreeNode parentNode) throws ParseException {
        final var template = "SYNTAX ERROR: '%s' was expected but '%s' was found.";
        final var errorMessage = template.formatted(expected.name(), lexer.getCurrentLexeme());
        codeGenerator.syntaxError(errorMessage, parentNode);
    }
}


