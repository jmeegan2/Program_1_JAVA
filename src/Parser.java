//  ************** REQUIRES JAVA 17 OR ABOVE! (https://adoptium.net/) ************** //

import java.util.logging.Logger;

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
    }


    private void StmtList(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode, "StmtList");
        while (lexer.currentToken() == Token.ID || lexer.currentToken() == Token.READ || lexer.currentToken() == Token.WRITE
                || lexer.currentToken() == Token.IF || lexer.currentToken() == Token.WHILE || lexer.currentToken() == Token.DO) {
            stmt(thisNode);
        }
        if (lexer.currentToken() == Token.ELSE || lexer.currentToken() == Token.FI || lexer.currentToken() == Token.OD) {
            // Stop the recursion when an ELSE, FI, or OD token is encountered
            EMPTY(thisNode);
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
            case DO:
                do_until_stmt(thisNode);
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
        if (lexer.currentToken() == Token.ELSE) {
            else_part(thisNode);
        }
        MATCH(thisNode, Token.FI);
    }


    private void fi_stmt(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode, "<fi_stmt>");

        if (lexer.currentToken() == Token.FI) {
            MATCH(thisNode, Token.FI);
            StmtList(thisNode);
        } else if (lexer.currentToken()==Token.IF || lexer.currentToken() == Token.$$) {
            EMPTY(thisNode);
        } else {
            stmt(thisNode);
        }

    }


    private void else_part(final TreeNode parentNode) throws ParseException {
        final TreeNode thisNode = codeGenerator.addNonTerminalToTree(parentNode, "<else_part>");

        MATCH(thisNode, Token.ELSE);
        StmtList(thisNode); // Add a new StmtList as a child of the current StmtList
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

