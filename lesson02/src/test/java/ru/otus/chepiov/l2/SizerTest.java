package ru.otus.chepiov.l2;

import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;


/**
 * Test cases for Sizer.
 *
 * @author <a href="mailto:a.kiekbaev@chepiov.org">Anvar Kiekbaev</a>
 */
@SuppressWarnings({"Convert2MethodRef", "RedundantStringConstructorCall"})
public class SizerTest {

    @Test
    public void jol() {
        final long emptyObject = Sizer.byJOL(() -> new Object(){});
        final long emptyStringWithoutIntern = Sizer.byJOL(() -> new String(""));
        final long emptyString = Sizer.byJOL(() -> "");
        final long emptyArrayList = Sizer.byJOL(() -> new ArrayList());
        final long emptyLinkedList = Sizer.byJOL(() -> new LinkedList());

        System.out.println("==== JOL ====");
        System.out.printf("Size of the empty object: %d\n", emptyObject);
        System.out.printf("Size of the empty string without interning: %d\n", emptyStringWithoutIntern);
        System.out.printf("Size of the empty string: %d\n", emptyString);
        System.out.printf("Size of the empty array list: %d\n", emptyArrayList);
        System.out.printf("Size of the empty linked list: %d\n", emptyLinkedList);
    }

    @Test
    public void jamm() {
        final long emptyObject = Sizer.byJAMM(() -> new Object(){});
        final long emptyStringWithoutIntern = Sizer.byJAMM(() -> new String(""));
        final long emptyString = Sizer.byJAMM(() -> "");
        final long emptyArrayList = Sizer.byJAMM(() -> new ArrayList());
        final long emptyLinkedList = Sizer.byJAMM(() -> new LinkedList());

        System.out.println("==== JAMM ====");
        System.out.printf("Size of the empty object: %d\n", emptyObject);
        System.out.printf("Size of the empty string without interning: %d\n", emptyStringWithoutIntern);
        System.out.printf("Size of the empty string: %d\n", emptyString);
        System.out.printf("Size of the empty array list: %d\n", emptyArrayList);
        System.out.printf("Size of the empty linked list: %d\n", emptyLinkedList);
    }

    @Test
    public void jmx() {
        final long emptyObject = Sizer.byMemAssumption(() -> new Object(){});
        final long emptyStringWithoutIntern = Sizer.byMemAssumption(() -> new String(""));
        final long emptyString = Sizer.byMemAssumption(() -> "");
        final long emptyArrayList = Sizer.byMemAssumption(() -> new ArrayList());
        final long emptyLinkedList = Sizer.byMemAssumption(() -> new LinkedList());

        System.out.println("==== Memory assumption ====");
        System.out.printf("Size of the empty object: %d\n", emptyObject);
        System.out.printf("Size of the empty string without interning: %d\n", emptyStringWithoutIntern);
        System.out.printf("Size of the empty string: %d\n", emptyString);
        System.out.printf("Size of the empty array list: %d\n", emptyArrayList);
        System.out.printf("Size of the empty linked list: %d\n", emptyLinkedList);
    }

}
