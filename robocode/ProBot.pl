atacar(Dist) :-Dist < 1000.
less(Pos, Val) :- Pos < Val.
equal(Pos, Val) :- Pos == Val.
greater(Pos, Val) :- Pos > Val.
checkWall(X,Y,W,H,Val):- less(X,Val); less(Y,Val); less(W-X,Val);less(H-Y,Val).
checkRadar(R):-R == 0.0.
checkName(N1, N2):- N1 == N2.
checkEnemy(Distance, EnemyDistance, Value, Name, EnemyName):- less(Distance,EnemyDistance - Value); checkName(Name,EnemyName).
checkFire(Heat, HeatValue, TurnRem, TurnValue ):- equal(Heat, HeatValue);less(TurnRem,TurnValue).
farEnemy(Dist, Val):- greater(Dist,Val).
closeEnemy(Dist,Val):- less(Dist, Val).