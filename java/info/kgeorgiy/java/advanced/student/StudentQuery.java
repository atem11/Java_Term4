package info.kgeorgiy.java.advanced.student;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Georgiy Korneev (kgeorgiy@kgeorgiy.info)
 */
public interface StudentQuery {
    /** Returns student {@link Student#getFirstName() first names}. */
    List<String> getFirstNames(final List<Student> students);

    /** Returns student {@link Student#getLastName() last names}. */
    List<String> getLastNames(final List<Student> students);

    /** Returns student {@link Student#getGroup() groups}. */
    List<String> getGroups(final List<Student> students);

    /** Returns student {@link Student#getGroup() groups}. */
    List<String> getFullNames(final List<Student> students);

    /** Returns distinct student {@link Student#getFirstName() first names} in alphabetical order. */
    Set<String> getDistinctFirstNames(final List<Student> students);

    /** Returns name of the student with minimal {@link Student#getId() id}. */
    String getMinStudentFirstName(final List<Student> students);

    /** Returns list of student sorted by {@link Student#getId() id}. */
    List<Student> sortStudentsById(Collection<Student> students);

    /**
     * Returns list of student sorted by name
     * (student are ordered by {@link Student#getLastName() lastName},
     * student with equal last names are ordered by {@link Student#getFirstName() firstName},
     * student having equal both last and first names are ordered by {@link Student#getId() id}.
     */
    List<Student> sortStudentsByName(Collection<Student> students);

    /** Returns list of student having specified first name. Students are ordered by name. */
    List<Student> findStudentsByFirstName(Collection<Student> students, String name);

    /** Returns list of student having specified last name. Students are ordered by name. */
    List<Student> findStudentsByLastName(Collection<Student> students, String name);

    /** Returns list of student having specified groups. Students are ordered by name. */
    List<Student> findStudentsByGroup(Collection<Student> students, String group);

    /** Returns map of group's student last names mapped to minimal first name. */
    Map<String, String> findStudentNamesByGroup(final Collection<Student> students, final String group);
}
