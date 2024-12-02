import java.util.Scanner;

class MemoryManager {
    private int totalMemory;  // 总内存大小
    private int[] memory;     // 内存空间，0代表空闲，1代表已分配

    // 构造函数，初始化内存
    public MemoryManager(int totalMemory) {
        this.totalMemory = totalMemory;
        this.memory = new int[totalMemory];  // 初始时全部是空闲
    }

    // 分配内存，支持不同的分配方式
    public int allocate(int processSize, String method) {
        switch (method) {
            case "first_fit":
                return firstFit(processSize);
            case "best_fit":
                return bestFit(processSize);
            case "worst_fit":
                return worstFit(processSize);
            default:
                System.out.println("Unknown allocation method.");
                return -1;
        }
    }

    // 最先适应法 (First-Fit)
    private int firstFit(int size) {
        for (int i = 0; i <= totalMemory - size; i++) {
            if (isSpaceAvailable(i, size)) {
                allocateMemory(i, size);
                return i;  // 返回分配的起始地址
            }
        }
        System.out.println("No space available for the process.");
        return -1;
    }

    // 最佳适应法 (Best-Fit)
    private int bestFit(int size) {
        int bestStart = -1;
        int bestSpace = Integer.MAX_VALUE;
        for (int i = 0; i <= totalMemory - size; i++) {
            if (isSpaceAvailable(i, size)) {
                int freeSpace = countFreeSpace(i, size);
                if (freeSpace < bestSpace) {
                    bestSpace = freeSpace;
                    bestStart = i;
                }
            }
        }
        if (bestStart != -1) {
            allocateMemory(bestStart, size);
            return bestStart;
        } else {
            System.out.println("No space available for the process.");
            return -1;
        }
    }

    // 最坏适应法 (Worst-Fit)
    private int worstFit(int size) {
        int worstStart = -1;
        int worstSpace = -1;
        for (int i = 0; i <= totalMemory - size; i++) {
            if (isSpaceAvailable(i, size)) {
                int freeSpace = countFreeSpace(i, size);
                if (freeSpace > worstSpace) {
                    worstSpace = freeSpace;
                    worstStart = i;
                }
            }
        }
        if (worstStart != -1) {
            allocateMemory(worstStart, size);
            return worstStart;
        } else {
            System.out.println("No space available for the process.");
            return -1;
        }
    }

    // 检查某个区域是否有足够的空闲空间
    private boolean isSpaceAvailable(int start, int size) {
        for (int i = start; i < start + size; i++) {
            if (memory[i] != 0) {
                return false;  // 发现已分配的空间
            }
        }
        return true;
    }

    // 分配内存
    private void allocateMemory(int start, int size) {
        for (int i = start; i < start + size; i++) {
            memory[i] = 1;  // 将内存标记为已分配
        }
        System.out.println("Memory allocated from address " + start + " to " + (start + size - 1));
    }

    // 回收内存
    public void deallocate(int start, int size) {
        for (int i = start; i < start + size; i++) {
            memory[i] = 0;  // 将内存标记为空闲
        }
        System.out.println("Memory deallocated from address " + start + " to " + (start + size - 1));
    }

    // 计算给定区域的空闲空间大小
    private int countFreeSpace(int start, int size) {
        int freeSpace = 0;
        for (int i = start; i < start + size; i++) {
            if (memory[i] == 0) {
                freeSpace++;
            }
        }
        return freeSpace;
    }

    // 显示内存状态
    public void displayMemory() {
        for (int i = 0; i < totalMemory; i++) {
            System.out.print(memory[i] + " ");
        }
        System.out.println();
    }
}

class MemorySimulation {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入内存总大小: ");
        int totalMemory = scanner.nextInt();

        // 初始化内存管理器
        MemoryManager memoryManager = new MemoryManager(totalMemory);

        System.out.print("请输入进程个数: ");
        int processCount = scanner.nextInt();

        int[] processSizes = new int[processCount];
        System.out.print("请输入每个进程的内存需求: ");
        for (int i = 0; i < processCount; i++) {
            processSizes[i] = scanner.nextInt();
        }

        System.out.println("选择分配算法: ");
        System.out.println("1. 最先适应法 (First-Fit)");
        System.out.println("2. 最佳适应法 (Best-Fit)");
        System.out.println("3. 最坏适应法 (Worst-Fit)");

        int choice = scanner.nextInt();
        String method = "";

        switch (choice) {
            case 1:
                method = "first_fit";
                break;
            case 2:
                method = "best_fit";
                break;
            case 3:
                method = "worst_fit";
                break;
            default:
                System.out.println("选择错误");
                return;
        }

        // 分配进程内存
        for (int i = 0; i < processCount; i++) {
            System.out.println("为进程 " + (i + 1) + " 分配内存 " + processSizes[i] + "MB");
            memoryManager.allocate(processSizes[i], method);
            memoryManager.displayMemory();
        }

        // 用户选择回收内存
        System.out.print("请输入回收进程编号 (0 表示不回收): ");
        int deallocateProcess = scanner.nextInt();
        if (deallocateProcess > 0 && deallocateProcess <= processCount) {
            int start = memoryManager.allocate(processSizes[deallocateProcess - 1], method); // 获取分配的起始地址
            memoryManager.deallocate(start, processSizes[deallocateProcess - 1]);
            memoryManager.displayMemory();
        }

        scanner.close();
    }
}
