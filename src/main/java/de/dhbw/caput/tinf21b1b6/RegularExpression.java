package de.dhbw.caput.tinf21b1b6;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

abstract class RegularExpression {
	
	private RegularExpression( ){
		super( );
	}
	
	static RegularExpression from( String string ){
		return Parser.parse( string );
	}
	
	abstract FiniteStateAutomaton generateFSA( );
	
	
	
	final static class EmptySet extends RegularExpression {
		public EmptySet( ){
			super( );
		}
		
		@Override
		public String toString( ){
			return "∅";
		}
		
		@Override
		FiniteStateAutomaton generateFSA( ){
			return new FiniteStateAutomaton( );
		}
	}
	
	final static class EmptyWord extends RegularExpression {
		public EmptyWord( ){
			super( );
		}
		
		@Override
		public String toString( ){
			return "ε";
		}
		
		@Override
		FiniteStateAutomaton generateFSA( ){
			FiniteStateAutomaton fsa = new FiniteStateAutomaton( );
			fsa.INITIAL_STATE.addEpsilonTransitionTo( fsa.ACCEPTING_STATE );
			return fsa;
		}
	}
	
	final static class Literal extends RegularExpression {
		private final char LITERAL;
		
		public Literal( char literal ){
			super( );
			LITERAL = literal;
		}
		
		@Override
		public String toString( ){
			return String.valueOf( LITERAL );
		}
		
		@Override
		FiniteStateAutomaton generateFSA( ){
			FiniteStateAutomaton fsa = new FiniteStateAutomaton( );
			fsa.INITIAL_STATE.addTransition( LITERAL, fsa.ACCEPTING_STATE );
			return fsa;
		}
	}
	
	final static class Union extends RegularExpression {
		private final RegularExpression FIRST;
		private final RegularExpression SECOND;
		
		public Union( RegularExpression first, RegularExpression second ){
			super( );
			FIRST = first;
			SECOND = second;
		}
		
		@Override
		public String toString( ){
			return String.format( "(%s|%s)", FIRST, SECOND );
		}
		
		@Override
		FiniteStateAutomaton generateFSA( ){
			FiniteStateAutomaton first = FIRST.generateFSA();
			FiniteStateAutomaton second = SECOND.generateFSA();
			FiniteStateAutomaton result = new FiniteStateAutomaton( );
			result.INITIAL_STATE.addEpsilonTransitionTo( first.INITIAL_STATE );
			result.INITIAL_STATE.addEpsilonTransitionTo( second.INITIAL_STATE );
			first.ACCEPTING_STATE.addEpsilonTransitionTo( result.ACCEPTING_STATE );
			second.ACCEPTING_STATE.addEpsilonTransitionTo( result.ACCEPTING_STATE );
			return result;
		}
	}
	
	final static class Concatenation extends RegularExpression {
		private final RegularExpression FIRST;
		private final RegularExpression SECOND;
		
		public Concatenation( RegularExpression first, RegularExpression second ){
			super( );
			FIRST = first;
			SECOND = second;
		}
		
		@Override
		public String toString( ){
			return String.format( "(%s.%s)", FIRST, SECOND );
		}
		
		@Override
		FiniteStateAutomaton generateFSA( ){
			FiniteStateAutomaton first = FIRST.generateFSA();
			FiniteStateAutomaton second = SECOND.generateFSA();
			FiniteStateAutomaton result = new FiniteStateAutomaton( );
			result.INITIAL_STATE.addEpsilonTransitionTo( first.INITIAL_STATE );
			first.ACCEPTING_STATE.addEpsilonTransitionTo( second.INITIAL_STATE );
			second.ACCEPTING_STATE.addEpsilonTransitionTo( result.ACCEPTING_STATE );
			return result;
		}
	}
	
	final static class KleeneStar extends RegularExpression {
		private final RegularExpression BASE;
		
		public KleeneStar( RegularExpression base ){
			super( );
			BASE = base;
		}
		
		@Override
		public String toString( ){
			return String.format( "(%s)*", BASE );
		}
		
		@Override
		FiniteStateAutomaton generateFSA( ){
			FiniteStateAutomaton base = BASE.generateFSA();
			FiniteStateAutomaton result = new FiniteStateAutomaton( );
			result.INITIAL_STATE.addEpsilonTransitionTo( base.INITIAL_STATE );
			result.INITIAL_STATE.addEpsilonTransitionTo( result.ACCEPTING_STATE );
			base.ACCEPTING_STATE.addEpsilonTransitionTo( base.INITIAL_STATE );
			base.ACCEPTING_STATE.addEpsilonTransitionTo( result.ACCEPTING_STATE );
			return result;
		}
	}

	final static class Concat extends RegularExpression {
		private final List<RegularExpression> asts = new ArrayList<>();

		public Concat(){
			super();
		}

		public void addRegularExpression(RegularExpression regex) {
			asts.add(regex);
		}

		public RegularExpression popLast() {
			return asts.remove(asts.size() - 1);
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();

			for (RegularExpression regex : asts) {
				builder.append(regex.toString());
			}

			return builder.toString();
		}

		@Override
		FiniteStateAutomaton generateFSA() {
			List<FiniteStateAutomaton> fsaAsts = asts.stream().map(RegularExpression::generateFSA).collect(Collectors.toList());

			FiniteStateAutomaton result = new FiniteStateAutomaton();

			if (!fsaAsts.isEmpty()) {
				result.INITIAL_STATE.addEpsilonTransitionTo(fsaAsts.get(0).INITIAL_STATE);

				FiniteStateAutomaton last = fsaAsts.get(0);

				for (int i = 1; i < fsaAsts.size(); i++) {
					FiniteStateAutomaton current = fsaAsts.get(i);
					last.ACCEPTING_STATE.addEpsilonTransitionTo(fsaAsts.get(i).INITIAL_STATE);

					last = current;
				}

				last.ACCEPTING_STATE.addEpsilonTransitionTo(result.ACCEPTING_STATE);
			}

			return result;
		}
	}

}
