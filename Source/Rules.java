
public class Rules {
	/* Sets of rules for the STCA. 
	 * 
	 * The first value is: 
	 * 0 if the rules are NOT rotation-symmetric,
	 * 1 if the rules are rotation-symmetric, 
	 * 
	 * The second value is: 
	 * 0 if the rules are not reflection-symmetric, 
	 * 1 if they are horizontally reflection-symmetric,
	 * 2 if they are vertically reflection-symmetric,
	 * 3 if they are horizontally AND vertically reflection-symmetric, but not compounded
	 * (i.e. given a rule, its horizontal reflection is also a rule, its vertical reflection is
	 * also a rule, BUT the vertical reflection of its horizontal reflection is NOT a rule)
	 * 4 if they are horizontally AND vertically reflection-symmetric AND compounded
	 *
	 * Note that 4 is not needed if also rotation-symmetric: 3 can be used instead as this 
	 * covers the same set of possibilities
	 * 4 is also never actually used by the runtime at the moment but is coded for generality 
	 *
	 * Each 16 values corresponds to a rule. The first 8 correspond to the source of a rule,
	 * and the second 8 correspond to the target of the rule. For each 8, the values represent
	 * the top, bottom, left and right of the center subcells, and the top, bottom, left, and right
	 * neighbouring subcells. When compared with the depiction of cells in Chapter 9 of the thesis,
	 * this corresponds to cells a,c,b,d,q,s,r,t in the case of the source of the rule, and
	 * e,g,f,h,u,w,v,x in the case of the target of the rule */
	static byte[] LeeHuangZhu2011={1,0,
		0,0,0,0,0,0,1,0, 0,0,0,1,0,0,0,0, 
		1,0,0,0,1,1,0,0, 1,0,0,1,1,0,0,0, 
		0,0,1,0,0,1,1,0, 0,0,1,1,0,0,1,0, 
		1,0,0,1,1,1,0,1, 1,0,1,1,1,0,0,1, 
		0,0,1,1,0,1,1,1, 1,1,0,1,1,1,0,0
	};

	static byte[] LeePeperAdachiMoritaMashiko2002={1,0,
		0,0,0,0,0,0,1,0, 0,0,0,1,0,0,0,0,
		0,1,0,0,0,1,1,0, 1,1,0,0,0,1,0,0,
		1,0,0,0,1,0,1,0, 1,0,0,1,1,0,0,0,
		0,0,1,1,0,1,1,1, 1,1,1,0,1,1,0,0
	};

	static byte[] LeePeperAdachiMorita2008={1,3,
		0,0,0,0,0,0,1,0, 0,0,0,1,0,0,0,0,
		1,0,0,0,1,0,1,0, 1,0,0,1,1,0,0,0,
		1,0,0,1,1,0,1,1, 1,1,0,1,1,0,0,1,
		1,0,1,0,0,1,1,0, 1,0,1,1,0,0,0,1,
		1,0,0,1,0,0,1,1, 1,1,1,0,0,0,1,0
	};

	static byte[] RS={1,0,
		0,0,0,0,1,0,0,0, 0,1,0,0,0,0,0,0, /* signal movement */
		1,0,0,0,1,1,0,0, 1,0,0,1,1,0,0,0, /* right turn */
		1,0,0,0,1,0,0,1, 1,1,0,0,1,0,0,0, /* left turn */
		1,0,1,0,1,1,1,0, 1,1,0,1,0,1,0,1 /* memory toggle */
	};

	static byte[] inverseRS={1,0,
		0,1,0,0,0,0,0,0, 0,0,0,0,1,0,0,0, /* inverse signal movement */
		1,0,0,1,1,0,0,0, 1,0,0,0,1,1,0,0, /* inverse right turn */
		1,1,0,0,1,0,0,0, 1,0,0,0,1,0,0,1, /* inverse left turn */
		1,1,0,1,0,1,0,1 , 1,0,1,0,1,1,1,0, /* inverse memory toggle */
	};

	static byte[] S={1,0,
		0,0,0,0,1,0,0,0, 0,1,0,0,0,0,0,0, /* signal movement */
		1,0,0,0,1,1,0,0, 1,0,0,1,1,0,0,0, /* right turn */
		1,0,0,0,1,0,0,1, 1,1,0,0,1,0,0,0, /* left turn */
		1,0,1,0,1,1,1,0, 1,1,0,1,0,1,0,1, /* memory toggle */
		1,0,0,0,1,0,1,0, 1,0,0,1,1,0,0,0 /* merge signal */
	};

	/* As not all rules are rotation-symmetric (Fork and Join rules)
	 * we must add ALL rules explicitly without rotation-symmetry */
	static byte[] NANBP={0,0,
		0,0,0,0,1,0,0,0, 0,1,0,0,0,0,0,0, /* signal R0 */
		0,0,0,0,0,0,0,1, 0,0,1,0,0,0,0,0, /* signal R1 */
		0,0,0,0,0,1,0,0, 1,0,0,0,0,0,0,0, /* signal R2 */
		0,0,0,0,0,0,1,0, 0,0,0,1,0,0,0,0, /* signal R3 */
		1,0,0,0,1,1,0,0, 1,0,0,1,1,0,0,0, /* right turn R0 */
		0,0,0,1,0,0,1,1, 0,1,0,1,0,0,0,1, /* right turn R1 */
		0,1,0,0,1,1,0,0, 0,1,1,0,0,1,0,0, /* right turn R2 */
		0,0,1,0,0,0,1,1, 1,0,1,0,0,0,1,0, /* right turn R3 */
		1,0,0,0,1,0,0,1, 1,1,0,0,1,0,0,0, /* left turn R0 */
		0,0,0,1,0,1,0,1, 0,0,1,1,0,0,0,1, /* left turn R1 */
		0,1,0,0,0,1,1,0, 1,1,0,0,0,1,0,0, /* left turn R2 */
		0,0,1,0,1,0,1,0, 0,0,1,1,0,0,1,0, /* left turn R3 */
		1,0,1,0,1,1,1,0, 1,1,0,1,0,1,0,1, /* memory toggle R0 */
		1,0,0,1,1,0,1,1, 0,1,1,1,0,1,1,0, /* memory toggle R1 */
		0,1,0,1,1,1,0,1, 1,1,1,0,1,0,1,0, /* memory toggle R2 */
		0,1,1,0,0,1,1,1, 1,0,1,1,1,0,0,1, /* memory toggle R3 */
		1,0,1,1,1,0,1,1, 1,1,0,0,1,1,0,0, /* fork -> join */
		1,1,0,0,1,1,0,0, 1,0,1,1,1,0,1,1, /* join -> fork */
		1,0,1,1,1,1,1,1, 1,1,1,1,1,1,0,0, /* fork to join producing fork outputs */
		1,1,0,0,1,1,1,1, 1,1,1,1,1,0,1,1, /* join to fork producing join output */
		0,0,0,0,1,0,1,0, 0,1,0,1,0,0,0,0, /* crossover R0 */
		0,0,0,0,1,0,0,1, 0,1,1,0,0,0,0,0, /* crossover R1 */
		0,0,0,0,0,1,0,1, 1,0,1,0,0,0,0,0, /* crossover R2 */
		0,0,0,0,0,1,1,0, 1,0,0,1,0,0,0,0 /* crossover R3 */
	}; 

	static byte[] inverseNANBP={0,0,
		0,1,0,0,0,0,0,0, 0,0,0,0,1,0,0,0, /* inverse signal R0 */
		0,0,1,0,0,0,0,0, 0,0,0,0,0,0,0,1, /* inverse signal R1 */
		1,0,0,0,0,0,0,0, 0,0,0,0,0,1,0,0, /* inverse signal R2 */
		0,0,0,1,0,0,0,0, 0,0,0,0,0,0,1,0, /* inverse signal R3 */
		1,0,0,1,1,0,0,0, 1,0,0,0,1,1,0,0, /* inverse right turn R0 */
		0,1,0,1,0,0,0,1, 0,0,0,1,0,0,1,1, /* inverse right turn R1 */
		0,1,1,0,0,1,0,0, 0,1,0,0,1,1,0,0, /* inverse right turn R2 */
		1,0,1,0,0,0,1,0, 0,0,1,0,0,0,1,1, /* inverse right turn R3 */
		1,1,0,0,1,0,0,0, 1,0,0,0,1,0,0,1, /* inverse left turn R0 */
		0,0,1,1,0,0,0,1, 0,0,0,1,0,1,0,1, /* inverse left turn R1 */
		1,1,0,0,0,1,0,0, 0,1,0,0,0,1,1,0, /* inverse left turn R2 */
		0,0,1,1,0,0,1,0, 0,0,1,0,1,0,1,0, /* inverse left turn R3 */
		1,1,0,1,0,1,0,1, 1,0,1,0,1,1,1,0, /* inverse memory toggle R0 */
		0,1,1,1,0,1,1,0, 1,0,0,1,1,0,1,1, /* inverse memory toggle R1 */
		1,1,1,0,1,0,1,0, 0,1,0,1,1,1,0,1, /* inverse memory toggle R2 */
		1,0,1,1,1,0,0,1, 0,1,1,0,0,1,1,1, /* inverse memory toggle R3 */
		1,1,0,0,1,1,0,0, 1,0,1,1,1,0,1,1, /* inverse fork -> join */
		1,0,1,1,1,0,1,1, 1,1,0,0,1,1,0,0, /* inverse join -> fork */
		1,1,1,1,1,1,0,0, 1,0,1,1,1,1,1,1, /* inverse fork to join producing fork outputs */
		1,1,1,1,1,0,1,1, 1,1,0,0,1,1,1,1, /* inverse join to fork producing join output */
		0,1,0,1,0,0,0,0, 0,0,0,0,1,0,1,0, /* crossover R0 */
		0,1,1,0,0,0,0,0, 0,0,0,0,1,0,0,1, /* crossover R1 */
		1,0,1,0,0,0,0,0, 0,0,0,0,0,1,0,1, /* crossover R2 */
		1,0,0,1,0,0,0,0, 0,0,0,0,0,1,1,0 /* crossover R3 */
	}; 

	static byte[] NAP={0,0,
		0,0,0,0,1,0,0,0, 0,1,0,0,0,0,0,0, /* signal R0 */
		0,0,0,0,0,0,0,1, 0,0,1,0,0,0,0,0, /* signal R1 */
		0,0,0,0,0,1,0,0, 1,0,0,0,0,0,0,0, /* signal R2 */
		0,0,0,0,0,0,1,0, 0,0,0,1,0,0,0,0, /* signal R3 */
		1,0,0,0,1,1,0,0, 1,0,0,1,1,0,0,0, /* right turn R0 */
		0,0,0,1,0,0,1,1, 0,1,0,1,0,0,0,1, /* right turn R1 */
		0,1,0,0,1,1,0,0, 0,1,1,0,0,1,0,0, /* right turn R2 */
		0,0,1,0,0,0,1,1, 1,0,1,0,0,0,1,0, /* right turn R3 */
		1,0,0,0,1,0,0,1, 1,1,0,0,1,0,0,0, /* left turn R0 */
		0,0,0,1,0,1,0,1, 0,0,1,1,0,0,0,1, /* left turn R1 */
		0,1,0,0,0,1,1,0, 1,1,0,0,0,1,0,0, /* left turn R2 */
		0,0,1,0,1,0,1,0, 0,0,1,1,0,0,1,0, /* left turn R3 */
		1,0,1,0,1,1,1,0, 1,1,0,1,0,1,0,1, /* memory toggle R0 */
		1,0,0,1,1,0,1,1, 0,1,1,1,0,1,1,0, /* memory toggle R1 */
		0,1,0,1,1,1,0,1, 1,1,1,0,1,0,1,0, /* memory toggle R2 */
		0,1,1,0,0,1,1,1, 1,0,1,1,1,0,0,1, /* memory toggle R3 */
		1,0,1,1,1,0,1,1, 1,1,0,0,1,1,0,0, /* fork -> join */
		1,1,0,0,1,1,0,0, 1,0,1,1,1,0,1,1, /* join -> fork */
		1,0,1,1,1,1,1,1, 1,1,1,1,1,1,0,0, /* fork to join producing fork outputs */
		1,1,0,0,1,1,1,1, 1,1,1,1,1,0,1,1, /* join to fork producing join output */
		0,0,0,0,1,0,1,0, 0,1,0,1,0,0,0,0, /* crossover R0 */
		0,0,0,0,1,0,0,1, 0,1,1,0,0,0,0,0, /* crossover R1 */
		0,0,0,0,0,1,0,1, 1,0,1,0,0,0,0,0, /* crossover R2 */
		0,0,0,0,0,1,1,0, 1,0,0,1,0,0,0,0, /* crossover R3 */
		1,0,0,0,1,0,1,0, 1,0,0,1,1,0,0,0, /* merge R0 */
		0,0,0,1,1,0,0,1, 0,1,0,1,0,0,0,1, /* merge R1 */
		0,1,0,0,0,1,0,1, 0,1,1,0,0,1,0,0, /* merge R2 */
		0,0,1,0,0,1,1,0, 1,0,1,0,0,0,1,0 /* merge R3 */
	};

	/* List of string names of the various STCA, in the order that they will
	 * appear in the program - list order must correspond with the sets of rules
	 * in the array below */
	static String[] names = {"2011 - Lee, Huang, Zhu",
		"2002 - Lee, Peper, Adachi, Morita, Mashiko",
		"2008 - Lee, Peper, Adachi, Morita",
		"RS", 
		"Inverse RS", 
		"S",
		"NANBP",
		"Inverse NANBP",
		"NAP"
	};

	/* List of (above defined) rule sets of the various STCA, corresponding with the
	 * order of names */
	static byte[][] rules = {LeeHuangZhu2011,
		LeePeperAdachiMoritaMashiko2002,
		LeePeperAdachiMorita2008,
		RS,
		inverseRS, 
		S, 
		NANBP, 
		inverseNANBP,
		NAP
	};
}
