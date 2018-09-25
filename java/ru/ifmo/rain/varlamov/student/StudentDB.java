package ru.ifmo.rain.varlamov.student;

import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentQuery;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

//java info.kgeorgiy.java.advanced.student.Tester StudentQuery ru.ifmo.rain.varlamov.student.StudentDB

public class StudentDB implements StudentQuery {
    @Override
    public List<String> getFirstNames(List<Student> students) {
        return getFields(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return getFields(students, Student::getLastName);
    }

    @Override
    public List<String> getGroups(List<Student> students) {
        return getFields(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return getFields(students, s -> s.getFirstName() + " " + s.getLastName());
    }

    private List<String> getFields(List<Student> students, Function<Student, String> s) {
        return students.stream().map(s).collect(Collectors.toList());
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        //return student.stream().map(Student::getFirstName).collect(Collectors.toCollection(TreeSet::new));
        return new TreeSet<>(getFields(students, Student::getFirstName));
    }

    @Override
    public String getMinStudentFirstName(List<Student> students) {
        //return sortStudentsById(students).get(0).getFirstName();
        return students.stream().min(Student::compareTo).map(Student::getFirstName).orElse("");
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return sortStudentsByField(students, Student::compareTo);
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return sortStudentsByField(students, Comparator.comparing(Student::getLastName, String::compareTo).
                thenComparing(Student::getFirstName, String::compareTo).
                thenComparingInt(Student::getId));
    }

    private List<Student> sortStudentsByField(Collection<Student> students, Comparator<? super Student> comparator) {
        return students.stream().sorted(comparator).collect(Collectors.toList());
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return findStudentsByField(students, s -> s.getFirstName().equals(name));
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return findStudentsByField(students, s -> s.getLastName().equals(name));
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, String group) {
        return findStudentsByField(students, s -> s.getGroup().equals(group));
    }

    private List<Student> findStudentsByField(Collection<Student> students, Predicate<? super Student> predicate) {
        return sortStudentsByName(students).stream().filter(predicate).collect(Collectors.toList());
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, String group) {
        //return students.stream().filter(s -> s.getGroup().equals(group)).collect(Collectors.toMap(Student::getLastName, Student::getFirstName, (s1, s2) -> (s1.compareTo(s2) > 0 ? s2 : s1)));
        return students.stream().filter(s -> s.getGroup().equals(group))
                .collect(Collectors.toMap(Student::getLastName, Student::getFirstName,
                        BinaryOperator.minBy(Comparator.naturalOrder())));
    }
}