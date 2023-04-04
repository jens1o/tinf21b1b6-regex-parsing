package de.dhbw.caput.tinf21b1b6;

public class ParsingContext {
    private ParsingState state;

    private RegularExpression.Concat ast = new RegularExpression.Concat();

    public ParsingContext(ParsingState state) {
        this.state = state;
    }

    public RegularExpression popLast() {
        return this.ast.popLast();
    }

    public void addToAst(RegularExpression regex) {
        this.ast.addRegularExpression(regex);
    }

    public ParsingState getState() {
        return state;
    }

    public RegularExpression.Concat getAst() {
        return ast;
    }

    @Override
    public String toString() {
        return String.format("(state=%s, ast=%s)", this.state.toString(), this.ast.toString());
    }
}
