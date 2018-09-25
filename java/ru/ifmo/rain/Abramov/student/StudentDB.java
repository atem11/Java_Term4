package ru.ifmo.rain.Abramov.student;

import info.kgeorgiy.java.advanced.student.Group;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentGroupQuery;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

//java info.kgeorgiy.java.advanced.student.Tester StudentGroupQuery ru.ifmo.rain.Abramov.student.StudentDB

public class StudentDB implements StudentGroupQuery {

    private static final Comparator<Student> NAME_COMP = Comparator.comparing(Student::getLastName).thenComparing(Student::getFirstName).thenComparing(Student::compareTo);

    private List<String> mapFunc(List<Student> s, Function<Student, String> func) {
        return s.stream().map(func).collect(Collectors.toList());
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return mapFunc(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return mapFunc(students, Student::getLastName);
    }

    @Override
    public List<String> getGroups(List<Student> students) {
        return mapFunc(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return mapFunc(students, s -> s.getFirstName() + " " + s.getLastName());
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return new TreeSet<>(mapFunc(students, Student::getFirstName));
    }

    @Override
    public String getMinStudentFirstName(List<Student> students) {
        return students.stream().min(Student::compareTo).map(Student::getFirstName).orElse("");
    }

    private Stream<Student> sortStudentName(Collection<Student> students) {
        return students.stream().sorted(NAME_COMP);
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return students.stream().sorted(Comparator.comparing(Student::getId)).collect(Collectors.toList());
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortStudentName(students).collect(Collectors.toList());
    }

    private List<Student> filterStudent(Collection<Student> students, Predicate<Student> filter) {
        return sortStudentName(students).filter(filter).collect(Collectors.toList());
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return filterStudent(students, s -> s.getFirstName().equals(name));
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return filterStudent(students, s -> s.getLastName().equals(name));
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, String group) {
        return filterStudent(students, s -> s.getGroup().equals(group));
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, String group) {
        return students.stream()
                .filter(s -> s.getGroup().equals(group))
                .collect(Collectors.toMap(
                        Student::getLastName,
                        Student::getFirstName,
                        BinaryOperator.minBy(Comparator.naturalOrder())
                ));
    }


    private Stream<Entry<String, List<Student>>> getStreamGroup(Collection<Student> students) {
        return students.stream().collect(Collectors.groupingBy(Student::getGroup, TreeMap::new, Collectors.toList())).entrySet().stream();
    }

    private List<Group> getListGroup(Stream<Entry<String, List<Student>>> groups, UnaryOperator<List<Student>> sort) {
        return groups.map(g -> new Group(g.getKey(), sort.apply(g.getValue()))).collect(Collectors.toList());
    }

    @Override
    public List<Group> getGroupsByName(Collection<Student> students) {
        return getListGroup(getStreamGroup(students), this::sortStudentsByName);
    }

    @Override
    public List<Group> getGroupsById(Collection<Student> students) {
        return getListGroup(getStreamGroup(students), this::sortStudentsById);
    }

    private String getFilterGroupElement(Stream<Entry<String, List<Student>>> groups, ToIntFunction<List<Student>> filter) {
        return groups.max(Comparator.comparingInt((Entry<String, List<Student>> g) -> filter.applyAsInt(g.getValue())).thenComparing(Entry::getKey, Collections.reverseOrder(String::compareTo))).map(Entry::getKey).orElse("");
    }

    @Override
    public String getLargestGroup(Collection<Student> students) {
        return getFilterGroupElement(getStreamGroup(students), List::size);
    }

    @Override
    public String getLargestGroupFirstName(Collection<Student> students) {
        return getFilterGroupElement(getStreamGroup(students), sL -> getDistinctFirstNames(sL).size());
    }
}
