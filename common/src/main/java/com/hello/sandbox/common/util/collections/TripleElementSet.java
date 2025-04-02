package com.hello.sandbox.common.util.collections;

import androidx.annotation.NonNull;
import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

public final class TripleElementSet<E> extends AbstractSet<E> implements Serializable {
  final E element1;
  final E element2;
  final E element3;

  public TripleElementSet(E object, E object2, E object3) {
    element1 = object;
    element2 = object2;
    element3 = object3;
  }

  @Override
  public boolean contains(Object object) {
    if (object == null) {
      return element1 == null || element2 == null || element3 == null;
    } else {
      return object.equals(element1) || object.equals(element2) || object.equals(element3);
    }
  }

  @Override
  public int size() {
    return 3;
  }

  @NonNull
  @Override
  public Iterator<E> iterator() {
    return new Iterator<E>() {
      int cur = 0;

      @Override
      public boolean hasNext() {
        return cur <= 2;
      }

      @Override
      public E next() {
        if (cur == 0) {
          cur = 1;
          return element1;
        } else if (cur == 1) {
          cur = 2;
          return element2;
        } else if (cur == 2) {
          cur = 3;
          return element3;
        }
        throw new NoSuchElementException();
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }
}
