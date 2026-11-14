/**
 * Concrete finite group implementations, permutation algebra, subgroup and coset structures,
 * quotient groups, and group actions.
 *
 * <p>This package provides concrete realizations of {@link lock14.algebra.structure.FiniteGroup},
 * including {@link lock14.algebra.group.SymmetricGroup}, {@link lock14.algebra.group.AlternatingGroup},
 * {@link lock14.algebra.group.CyclicGroup}, and {@link lock14.algebra.group.DihedralGroup}, backed by
 * immutable {@link lock14.algebra.group.Permutation} elements where applicable. It also provides
 * structural constructions for {@link lock14.algebra.group.Subgroup}, {@link lock14.algebra.group.Coset},
 * {@link lock14.algebra.group.QuotientGroup}, and left {@link lock14.algebra.group.GroupAction}
 * computations such as orbits, stabilizers, and Burnside's lemma.
 *
 * <h2>Example</h2>
 * {@snippet class="lock14.algebra.group.GroupExamplesTest" region="quotient-and-burnside"}
 */
package lock14.algebra.group;
