package de.dhbw.caput.tinf21b1b6;

public enum ParsingState {
    GLOBAL,
    IN_GROUP,
    IN_UNION,
    IN_CONCAT;

    /**
     * @return whether this state may be collapsed with its parent at the end of input
     */
    boolean canBeCollapsedAtEnd() {
        switch (this) {
            case IN_GROUP: // needs a closing parenthese at the end
            case GLOBAL: // must never be collapsed as there is only one GLOBAL state
                return false;
            default:
                return true;
        }
    }
}
