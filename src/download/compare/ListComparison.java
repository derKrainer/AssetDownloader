package download.compare;

import java.util.ArrayList;
import java.util.List;

public class ListComparison<T>
{
  public List<T> newItems = new ArrayList<>();
  public List<T> oldItems = new ArrayList<>();
  public List<T> sameItems = new ArrayList<>();

  public ListComparison(List<T> oldList, List<T> newList)
  {
    this.compareLists(oldList, newList);
  }

  private void compareLists(List<T> oldList, List<T> newList)
  {
    for (T item : oldList)
    {
      if (newList.contains(item))
      {
        sameItems.add(item);
      }
      else
      {
        oldItems.add(item);
      }
    }

    for (T item : newList)
    {
      if (!sameItems.contains(item))
      {
        newItems.add(item);
      }
    }
  }
}