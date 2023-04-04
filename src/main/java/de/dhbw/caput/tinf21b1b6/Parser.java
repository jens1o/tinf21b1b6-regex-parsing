package de.dhbw.caput.tinf21b1b6;

import java.util.Stack;

final class Parser {

    private final char[] inputString;
    private int currentIndex = -1;

    private final Stack<ParsingContext> contextStack = new Stack<>();

    private int charAccessCounter = 0;

    Parser(String inputString) {
        System.out.println("input: " + inputString + "\n");
        this.inputString = inputString.toCharArray();

        // create initial parsing context
        this.contextStack.push(new ParsingContext(ParsingState.GLOBAL));
    }

    public static RegularExpression parse(String string) {
        return new Parser(string).parse();
    }

    private RegularExpression parse() {
        while (true) {
            // advance to next character
            if (!bump()) {
                // reached EOI, we're done
                break;
            }

            System.out.println("reading: " + getChar());

            switch (getChar()) {
                case '(':
                    openGroup();
                    break;
                case ')':
                    closeGroup();
                    break;
                case '|':
                    openUnion();
                    break;
                case '·':
                    openConcat();
                    break;
                case '*':
                    handleKleene();
                    break;
                case 'ε':
                    handleEmptyWord();
                    break;
                default:
                    handleLiteral();
                    break;
            }
        }

        while (contextStack.peek().getState().canBeCollapsedAtEnd()) {
            collapseContext(contextStack.peek());
        }

        if (contextStack.size() != 1) {
            throw new RuntimeException("incollapsable context(s) at end of input?");
        }

        ParsingContext globalContext = contextStack.pop();

        System.out.println("lexed result: " + globalContext);
        System.out.println("access counter: " + charAccessCounter);

        return globalContext.getAst();
    }

    private void handleLiteral() {
        ParsingContext currentContext = contextStack.peek();

        currentContext.addToAst(new RegularExpression.Literal(getChar()));
    }

    private void handleEmptyWord() {
        if (getChar() != 'ε') {
            throw new RuntimeException("called handleEmptyWord(), but not reading ε");
        }

        ParsingContext currentContext = contextStack.peek();

        currentContext.addToAst(new RegularExpression.EmptyWord());
    }

    private void handleKleene() {
        if (getChar() != '*') {
            throw new RuntimeException("handleKleene() called without *");
        }

        ParsingContext currentContext = contextStack.peek();

        currentContext.addToAst(new RegularExpression.KleeneStar(currentContext.popLast()));
    }

    private void collapseContext(ParsingContext currentContext) {
        switch (currentContext.getState()) {
            case IN_UNION:
                closeUnion();
                break;
            case IN_CONCAT:
                closeConcat();
                break;
            default:
                throw new RuntimeException("no strategy defined to collapse state " + currentContext.getState().toString());
        }
    }

    private void openGroup() {
        if (getChar() != '(') {
            throw new RuntimeException("openGroup() was called without opening parenthese");
        }

        // open new context
        ParsingContext context = new ParsingContext(ParsingState.IN_GROUP);
        contextStack.push(context);
    }


    private void closeGroup() {
        if (getChar() != ')') {
            throw new RuntimeException("closeGroup() was called without closing parenthese");
        }

        // a closing group wins over every other things, so collapse all other contexts
        // until we get to the closest group
        while (true) {
            ParsingContext context = contextStack.peek();

            if (context == null) {
                throw new RuntimeException("got null context??!?!");
            }

            if (context.getState() == ParsingState.IN_GROUP) {
                ParsingContext groupContext = contextStack.pop();

                // let the now top-most parsing context retrieve this ast
                contextStack.peek().addToAst(groupContext.getAst());
                break;
            }

            collapseContext(context);
        }
    }

    private void openUnion() {
        if (getChar() != '|') {
            throw new RuntimeException("openUnion() was called without |");
        }

        // open new context
        ParsingContext context = new ParsingContext(ParsingState.IN_UNION);
        contextStack.push(context);
    }


    private void closeUnion() {
        ParsingContext unionContext = contextStack.pop();
        if (unionContext == null || unionContext.getState() != ParsingState.IN_UNION) {
            throw new RuntimeException("closeUnion() was called, but not in union");
        }

        // create UNION-Token between the AST of this union and the last AST
        ParsingContext currentContext = contextStack.peek();
        currentContext.addToAst(new RegularExpression.Union(currentContext.popLast(), unionContext.getAst()));
    }

    private void openConcat() {
        if (getChar() != '·') {
            throw new RuntimeException("openConcat() was called without ·");
        }

        // open new context
        ParsingContext context = new ParsingContext(ParsingState.IN_CONCAT);
        contextStack.push(context);
    }

    private void closeConcat() {
        ParsingContext concatContext = contextStack.pop();
        if (concatContext == null || concatContext.getState() != ParsingState.IN_CONCAT) {
            throw new RuntimeException("closeConcat() was called, but not in concat");
        }

        // create CONCAT-Token between the AST of this union and the last AST
        ParsingContext currentContext = contextStack.peek();
        currentContext.addToAst(new RegularExpression.Concatenation(currentContext.popLast(), concatContext.getAst()));
    }


    /**
     * Advance to the next char.
     *
     * @return false if end of input is reached
     */
    private boolean bump() {
        currentIndex++;

        return !isEOI();
    }

    /**
     * @return true if reached end of input
     */
    private boolean isEOI() {
        return currentIndex >= inputString.length;
    }

    private char getChar() {
        charAccessCounter++;

        return inputString[currentIndex];
    }

}
