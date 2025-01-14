// -*- tab-width:2 ; indent-tabs-mode:nil -*-
//:: cases ListAppendASyncDefInline
//:: tools silicon
//:: verdict Pass

/**
  This example shows the usefulness of the inline modifier.
*/

final class List {
  public int val;
  public List next;

  /*@
  resource state() =
    Perm(val,1) ** Perm(next,1) ** next->state();

  requires state();
  pure seq<int> contents() = \unfolding state() \in
      (next == null ? [val] : val :: next.contents());

  inline resource list(seq<int> c) = state() ** contents() == c;
  @*/

  /*@
  ensures list([v]);
  @*/
  public List(int v){
    val=v;
    next=null;
    //@ fold state();
  }

  /*@
  given    seq<int> L1;
  given    seq<int> L2;
  requires this.list(L1);
  requires l!=null ** l.list(L2);
  ensures  this.list(L1+L2);
  @*/
  public void append_rec(List l){
    //@ unfold state();
    if (next==null) {
        next=l;
    } else {
        //@ ghost seq<int> tmp = next.contents();
        next.append_rec(l)/*@ given { L1 = tmp, L2 = L2 } @*/;
    }
    //@ fold state();
  }
}