package it.finmatica.dmServer.jdmsfx;

import java.util.Vector;

public class QuickSort {

  public QuickSort(Vector elements)
  { if (! elements.isEmpty())
    { this.quickSort(elements, 0, elements.size()-1);
    }
  }

  private void quickSort(Vector elements, int lowIndex, int highIndex)
  { int lowToHighIndex;
    int highToLowIndex; 
    int pivotIndex;
    String pivotValue;  // values are Strings in this demo, change to suit your application
    String lowToHighValue;
    String highToLowValue;
    NodeTree NlowToHighValue;
    NodeTree NhighToLowValue;
    String parking;
    int newLowIndex;
    int newHighIndex;
    int compareResult;

    lowToHighIndex = lowIndex;
    highToLowIndex = highIndex;
    /** Choose a pivot, remember it's value
     *  No special action for the pivot element itself.
     *  It will be treated just like any other element.
     */
    pivotIndex = (lowToHighIndex + highToLowIndex) / 2;
    pivotValue = ((NodeTree)elements.elementAt(pivotIndex)).getTextQuickSort().toUpperCase();
  
    /** Split the Vector in two parts.
     *
     *  The lower part will be lowIndex - newHighIndex,
     *  containing elements <= pivot Value
     *
     *  The higher part will be newLowIndex - highIndex,
     *  containting elements >= pivot Value
     */
    newLowIndex = highIndex + 1;
    newHighIndex = lowIndex - 1;
    // loop until low meets high
    while ((newHighIndex + 1) < newLowIndex) // loop until partition complete
    { // loop from low to high to find a candidate for swapping      
      lowToHighValue = ((NodeTree)elements.elementAt(lowToHighIndex)).getTextQuickSort().toUpperCase();
      
      NlowToHighValue = (NodeTree)elements.elementAt(lowToHighIndex);
      while (lowToHighIndex < newLowIndex
        & lowToHighValue.compareTo(pivotValue)<0 )
      { newHighIndex = lowToHighIndex; // add element to lower part
        lowToHighIndex ++;
        lowToHighValue = ((NodeTree)elements.elementAt(lowToHighIndex)).getTextQuickSort().toUpperCase();
        NlowToHighValue = (NodeTree)elements.elementAt(lowToHighIndex);
      }

      // loop from high to low find other candidate for swapping
      highToLowValue = ((NodeTree)elements.elementAt(highToLowIndex)).getTextQuickSort().toUpperCase();
      NhighToLowValue = (NodeTree)elements.elementAt(highToLowIndex);
      while (newHighIndex <= highToLowIndex
        & (highToLowValue.compareTo(pivotValue)>0)
        )
      { newLowIndex = highToLowIndex; // add element to higher part
        highToLowIndex --;
        highToLowValue = ((NodeTree)elements.elementAt(highToLowIndex)).getTextQuickSort().toUpperCase();
        NhighToLowValue = (NodeTree)elements.elementAt(highToLowIndex);
      }

      // swap if needed
      if (lowToHighIndex == highToLowIndex) // one last element, may go in either part
      { newHighIndex = lowToHighIndex; // move element arbitrary to lower part
      }
      else if (lowToHighIndex < highToLowIndex) // not last element yet
      { compareResult = lowToHighValue.compareTo(highToLowValue);
        if (compareResult >= 0) // low >= high, swap, even if equal
        { parking = lowToHighValue;
          elements.setElementAt(NhighToLowValue, lowToHighIndex);
          elements.setElementAt(NlowToHighValue/*parking*/, highToLowIndex);

          newLowIndex = highToLowIndex;
          newHighIndex = lowToHighIndex;

          lowToHighIndex ++;
          highToLowIndex --;
        }
      }
    }

    // Continue recursion for parts that have more than one element
    if (lowIndex < newHighIndex)
    { this.quickSort(elements, lowIndex, newHighIndex); // sort lower subpart
    }
    if (newLowIndex < highIndex)
    { this.quickSort(elements, newLowIndex, highIndex); // sort higher subpart
    }
  }
}