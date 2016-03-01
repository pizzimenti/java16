import java.util.*;
import org.sql2o.*;

public class Task {
  private int id;
  private String description;

  public Task(String description) {
    this.description = description;
  }

  public int getId() {
    return id;
  }

  public String getDescription() {
    return description;
  }

  public static List<Task> all() {
    String sql = "SELECT id, description FROM tasks";
    try(Connection con = DB.sql2o.open()) {
      return con.createQuery(sql).executeAndFetch(Task.class);
    }
  }

  @Override
  public boolean equals(Object otherTask){
    if (!(otherTask instanceof Task)) {
      return false;
    } else {
      Task newTask = (Task) otherTask;
      return this.getDescription().equals(newTask.getDescription()) &&
             this.getId() == newTask.getId();
    }
  }

  public void save() {
    try(Connection con = DB.sql2o.open()) {
      String sql = "INSERT INTO Tasks (description, done) VALUES (:description, true)";
      this.id = (int) con.createQuery(sql, true)
        .addParameter("description", this.description)
        .executeUpdate()
        .getKey();
    }
  }

  public static Task find(int id) {
    try(Connection con = DB.sql2o.open()) {
      String sql = "SELECT id, description FROM tasks WHERE id = :id";
      return con.createQuery(sql)
        .addParameter("id", id)
        .executeAndFetchFirst(Task.class);
    }
  }

  public void addCategory(Category category) {
    try(Connection con = DB.sql2o.open()) {
      String sql = "INSERT INTO categories_tasks (category_id, task_id) VALUES (:category_id, :task_id)";
      con.createQuery(sql)
        .addParameter("category_id", category.getId())
        .addParameter("task_id", this.getId())
        .executeUpdate();
    }
  }

  public ArrayList<Category> getCategories(){

    //create a List object called categoryIds containing Integer objects which represent each matching category_id
    try(Connection con = DB.sql2o.open()) {
      String categoryIDQuery = "SELECT category_id FROM categories_tasks WHERE task_id = :task_id;";
      List<Integer> categoryIds = con.createQuery(categoryIDQuery)
        .addParameter("task_id", this.getId())
        .executeAndFetch(Integer.class);

      //create an empty ArrayList object that will hold Category objects
      //call it categories
      ArrayList<Category> categories = new ArrayList<Category>();

      //for each element (we will call them categoryId) in categoryIds
      //select the record from categories where id is the categoryId
      //create a Category object from that selection
      //then insert that Category object into the ArrayList<Category> categories
      for (Integer categoryId : categoryIds) {
        String categoryQuery = "SELECT * FROM categories WHERE id = :categoryId;";
        Category category = con.createQuery(categoryQuery)
          .addParameter("categoryId", categoryId)
          .executeAndFetchFirst(Category.class);
        categories.add(category);
      }
      //return ArrayList<Category> categories
      return categories;
    }
  }

  public void delete() {
    try(Connection con = DB.sql2o.open()) {
      String deleteQuery = "DELETE FROM tasks WHERE id = :id;";
      con.createQuery(deleteQuery)
        .addParameter("id", this.id)
        .executeUpdate();

      String joinDeleteQuery = "DELETE FROM categories_tasks WHERE task_id = :taskId";
        con.createQuery(joinDeleteQuery)
          .addParameter("taskId", this.getId())
          .executeUpdate();
    }
  }

  public void toggleDone() {
    if(this.done == false) {
      this.done = true;
    } else if (this.done == true) {
      this.done = false;
    }
  }
}
