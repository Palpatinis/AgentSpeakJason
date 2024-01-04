// mars robot 1

/* Initial beliefs */

at(P) :- pos(P,X,Y) & pos(r1,X,Y).

/* Initial goal */

!go_to(cust).

/* Plans */

+!go_to(cust):not customer(r1)
   <- move(custs);
   !go_to(cust).
+!go_to(cust).



+customer(r1) : not .desire(carry_to(r2))
   <- !carry_to(r2).
+customer(r3) : not .desire(carry_to(r2))
   <- !carry_to(r2).
+customer(r4) : not .desire(carry_to(r2))
   <- !carry_to(r2).
+customer(r5) : not .desire(carry_to(r2))
   <- !carry_to(r2).

+!carry_to(R)
   <- .drop_desire(go_to(cust)); // stop checking 
      
      // remember where to go back
      ?pos(r1,X,Y);
      -+pos(last,X,Y);

      // carry customer to r2
      !take(cust,R);

      // goes back and continue to check
      !at(R);
      !!go_to(cust).
+!take(S,L)
   <- !ensure_pick(S);
      !at(L);
      drop(S).

+!ensure_pick(S) : customer(r1)
   <- pick(cust);
      !ensure_pick(S).
+!ensure_pick(_).

+!at(L) : at(L).
+!at(L) <- ?pos(L,X,Y);
           move_towards(X,Y);
           !at(L).

+customer(r2) : true <- burn(cust).
+customer(r3) : true <- burn(cust).
+customer(r4) : true <- burn(cust).
+customer(r5) : true <- burn(cust).

