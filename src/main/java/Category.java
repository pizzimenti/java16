import java.util.*;
import org.sql2o.*;
import java.util.List;
import java.util.ArrayList;

public class Category {
  private int id;
  private String name;


  public Category(String name) {
    this.name = name;
  }

  public int getId() {
    return id;
  }

  public String getName() {
    return name;
  }

  public static List<Category> all() {
    String sql = "SELECT id, name FROM categories";
    try(Connection con = DB.sql2o.open()) {
      return con.createQuery(sql).executeAndFetch(Category.class);
    }
  }

  @Override
  public boolean equals(Object otherCategory){
    if (!(otherCategory instanceof Category)) {
      return false;
    } else {
      Category newCategory = (Category) otherCategory;
      return this.getName().equals(newCategory.getName()) &&
             this.getId() == newCategory.getId();
    }
  }

  public void save() {
    try(Connection con = DB.sql2o.open()) {
      String sql = "INSERT INTO categories (name) VALUES (:name)";
      this.id = (int) con.createQuery(sql, true)
        .addParameter("name", this.name)
        .executeUpdate()
        .getKey();
    }
  }

  public static Category find(int id) {
    try(Connection con = DB.sql2o.open()) {
      String sql = "SELECT * FROM categories WHERE id = :id";
      return con.createQuery(sql)
        .addParameter("id", id)
        .executeAndFetchFirst(Category.class);
    }
  }

  public void addTask(Task task) {
    try(Connection con = DB.sql2o.open()) {
      String sql = "INSERT INTO categories_tasks (category_id, task_id) VALUES (:category_id, :task_id)";
      con.createQuery(sql)
        .addParameter("category_id", this.getId())
        .addParameter("task_id", task.getId())
        .executeUpdate();
    }
  }

  public ArrayList<Task> getTasks(){

    //create a List object called taskIds containing Integer objects which represent each matching category_id
    try(Connection con = DB.sql2o.open()) {
      String taskIDQuery = "SELECT task_id FROM categories_tasks WHERE category_id = :category_id;";
      List<Integer> taskIds = con.createQuery(taskIDQuery)
        .addParameter("category_id", this.getId())
        .executeAndFetch(Integer.class);

      //create an empty ArrayList object that will hold Task objects
      //call it tasks
      ArrayList<Task> tasks = new ArrayList<Task>();

      //for each element (we will call them taskId) in taskIds
      //select the record from tasks where id is the taskId
      //create a Task object from that selection
      //then insert that Task object into the ArrayList<Task> tasks
      for (Integer taskId : taskIds) {
        String taskQuery = "SELECT * FROM tasks WHERE id = :taskId;";
        Task task = con.createQuery(taskQuery)
          .addParameter("taskId", taskId)
          .executeAndFetchFirst(Task.class);
        tasks.add(task);
      }
      //return ArrayList<Task> tasks
      return tasks;
    }
  }

  public void delete(){
    try(Connection con = DB.sql2o.open()){
      String deleteQuery = "DELETE FROM categories WHERE id = :id";
        con.createQuery(deleteQuery)
          .addParameter("id", id)
          .executeUpdate();

      String joinDeleteQuery = "DELETE FROM categories_tasks WHERE category_id = :categoryId";
        con.createQuery(joinDeleteQuery)
          .addParameter("categoryId", this.getId())
          .executeUpdate();
    }
  }
}
