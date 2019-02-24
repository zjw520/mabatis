package com.itheima.dao;

import com.itheima.domel.Book;

import java.util.List;

public interface IBookDao {
    List<Book> findAll();
}
