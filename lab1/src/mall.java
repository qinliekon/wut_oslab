import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

class mall {
    private int totalMemory;  // 总内存大小
    private int[] memory;     // 内存空间，0代表空闲，1代表已分配
    private HashMap<Integer, MemoryBlock> processMap = new HashMap<>(); // 存储进程编号和起始地址、大小
    private List<MemoryBlock> freeMemoryBlocks = new ArrayList<>(); // 空闲内存区域表

    // 内存块类，用于保存进程分配的信息
    static class MemoryBlock {
        int start;
        int size;

        MemoryBlock(int start, int size) {
            this.start = start;
            this.size = size;
        }
    }

    public mall(int totalMemory) {
        this.totalMemory = totalMemory;
        this.memory = new int[totalMemory];  // 初始时全部是空闲
        // 初始化一个空闲区域表，整个内存为一个大空闲块
        freeMemoryBlocks.add(new MemoryBlock(0, totalMemory));
    }

    // 分配内存，支持不同的分配方式
    public int allocate(int processID, int processSize, String method) {
        int start = -1;
        switch (method) {
            case "first_fit":
                start = firstFit(processSize);
                break;
            case "best_fit":
                start = bestFit(processSize);
                break;
            case "worst_fit":
                start = worstFit(processSize);
                break;
            default:
                System.out.println("未知的分配方式.");
        }
        if (start != -1) {
            processMap.put(processID, new MemoryBlock(start, processSize)); // 记录进程的分配信息
        }
        return start;
    }

    // 最先适应法 (First-Fit)
    private int firstFit(int size) {
        for (int i = 0; i < freeMemoryBlocks.size(); i++) {
            MemoryBlock block = freeMemoryBlocks.get(i);
            if (block.size >= size) {
                int start = block.start;
                allocateMemory(start, size);

                // 更新空闲区域表，分配内存后更新空闲块
                if (block.size == size) {
                    freeMemoryBlocks.remove(i); // 完全分配，移除该空闲块
                } else {
                    // 如果空闲块大小大于分配的内存，则更新剩余空闲块
                    block.start += size;
                    block.size -= size;
                }

                return start;
            }
        }
        System.out.println("无可用空间.");
        return -1;
    }

    // 最佳适应法 (Best-Fit)
    private int bestFit(int size) {
        int bestStart = -1;
        int bestSpace = Integer.MAX_VALUE;
        for (int i = 0; i < freeMemoryBlocks.size(); i++) {
            MemoryBlock block = freeMemoryBlocks.get(i);
            if (block.size >= size) {
                int freeSpace = block.size;
                if (freeSpace < bestSpace) {
                    bestSpace = freeSpace;
                    bestStart = block.start;
                }
            }
        }
        if (bestStart != -1) {
            allocateMemory(bestStart, size);

            // 更新空闲区域表
            int finalBestStart = bestStart;
            int finalBestSpace = bestSpace;
            MemoryBlock bestBlock = freeMemoryBlocks.stream()
                    .filter(b -> b.start == finalBestStart && b.size == finalBestSpace)
                    .findFirst()
                    .orElse(null);
            if (bestBlock != null) {
                freeMemoryBlocks.remove(bestBlock);  // 移除最合适的空闲块
                if (bestBlock.size > size) {
                    bestBlock.start += size;
                    bestBlock.size -= size;
                    freeMemoryBlocks.add(bestBlock);
                }
            } // 如果空闲块没用完，那么把碎片从这里加回来
            return bestStart;
        } else {
            System.out.println("无可用空间.");
            return -1;
        }
    }

    // 最坏适应法 (Worst-Fit)
    private int worstFit(int size) {
        int worstStart = -1;
        int worstSpace = -1;
        for (int i = 0; i < freeMemoryBlocks.size(); i++) {
            MemoryBlock block = freeMemoryBlocks.get(i);
            if (block.size >= size) {
                int freeSpace = block.size;
                if (freeSpace > worstSpace) {
                    worstSpace = freeSpace;
                    worstStart = block.start;
                }
            }
        }
        if (worstStart != -1) {
            allocateMemory(worstStart, size);

            // 更新空闲区域表
            int finalWorstStart = worstStart;
            int finalWorstSpace = worstSpace;
            MemoryBlock worstBlock = freeMemoryBlocks.stream()
                    .filter(b -> b.start == finalWorstStart && b.size == finalWorstSpace)
                    .findFirst()
                    .orElse(null);
            if (worstBlock != null) {
                freeMemoryBlocks.remove(worstBlock);  // 移除最坏的空闲块
                if (worstBlock.size > size) {
                    worstBlock.start += size;
                    worstBlock.size -= size;
                    freeMemoryBlocks.add(worstBlock);
                }
            } // 把碎片添加回表
            return worstStart;
        } else {
            System.out.println("无可用空间.");
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
        System.out.println("内存地址从 " + start + " 到 " + (start + size - 1));
    }

    // 回收内存
    public void deallocate(int processID) {
        if (processMap.containsKey(processID)) {
            MemoryBlock block = processMap.get(processID);
            // 回收分配的内存
            for (int i = block.start; i < block.start + block.size; i++) {
                memory[i] = 0;  // 将内存标记为空闲
            }
            // 从进程映射中移除该进程
            processMap.remove(processID);
            System.out.println("进程 " + processID + " 的内存已回收.");

            // 将回收的内存块添加到空闲区域表
            freeMemoryBlocks.add(new MemoryBlock(block.start, block.size));
            // 合并空闲块
            mergeFreeBlocks();
        } else {
            System.out.println("无效的进程ID，无法回收内存.");
        }
    }
    private void mergeFreeBlocks() {
        // 按照起始地址排序空闲块
        freeMemoryBlocks.sort((a, b) -> Integer.compare(a.start, b.start));

        List<MemoryBlock> mergedBlocks = new ArrayList<>();

        // 遍历空闲块，合并相邻的空闲块
        for (int i = 0; i < freeMemoryBlocks.size(); i++) {
            if (mergedBlocks.isEmpty()) {
                mergedBlocks.add(freeMemoryBlocks.get(i));
            } else {
                MemoryBlock lastBlock = mergedBlocks.get(mergedBlocks.size() - 1);
                MemoryBlock currentBlock = freeMemoryBlocks.get(i);

                // 判断当前块是否和上一个块相邻，如果相邻则合并
                if (lastBlock.start + lastBlock.size == currentBlock.start) {
                    lastBlock.size += currentBlock.size;  // 合并
                } else {
                    mergedBlocks.add(currentBlock);  // 不相邻，直接添加
                }
            }
        }

        // 更新空闲区域表
        freeMemoryBlocks = mergedBlocks;
        System.out.println("空闲区域合并完成.");
    }
    // 显示内存状态
    public void displayMemory() {
        for (int i = 0; i < totalMemory; i++) {
            System.out.print(memory[i] == 0 ? "0" : "1"); // '0' 表示空闲，'1' 表示已分配
        }
        System.out.println();
    }

    // 显示当前所有进程的内存信息
    public void displayProcesses() {
        System.out.println("当前所有进程：");
        if (processMap.isEmpty()) {
            System.out.println("没有正在运行的进程.");
        } else {
            for (Integer processID : processMap.keySet()) {
                MemoryBlock block = processMap.get(processID);
                System.out.println("进程ID: " + processID + ", 起始地址: " + block.start + ", 大小: " + block.size);
            }
        }
    }

    // 显示空闲内存区域表
    public void displayFreeMemoryBlocks() {
        System.out.println("空闲内存区域表：");
        if (freeMemoryBlocks.isEmpty()) {
            System.out.println("没有空闲内存区域.");
        } else {
            for (MemoryBlock block : freeMemoryBlocks) {
                System.out.println("起始地址: " + block.start + ", 大小: " + block.size);
            }
        }
    }

}

class MemorySimulation {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // 输入内存总大小
        System.out.print("请输入内存总大小: ");
        int totalMemory = scanner.nextInt();
//        mall.MemoryBlock totalblock = new mall.MemoryBlock(0,totalMemory);
        mall memoryManager = new mall(totalMemory);

        int processID = 1;  // 进程ID

        // 主菜单循环
        while (true) {
            System.out.println("\n*** 内存管理模拟器 ***");
            System.out.println("1. 显示内存状态");
            System.out.println("2. 分配内存");
            System.out.println("3. 回收内存");
            System.out.println("4. 退出");
            System.out.println("5. 显示所有进程");
            System.out.println("6. 空闲内存区域表");
            System.out.print("请输入操作选项: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:  // 显示内存状态
                    memoryManager.displayMemory();
                    break;

                case 2:  // 分配内存
                    System.out.println("选择分配算法: ");
                    System.out.println("1. 最先适应法 (First-Fit)");
                    System.out.println("2. 最佳适应法 (Best-Fit)");
                    System.out.println("3. 最坏适应法 (Worst-Fit)");
                    int methodChoice = scanner.nextInt();
                    String method;
                    switch (methodChoice) {
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
                            continue;
                    }

                    System.out.print("请输入进程的内存需求: ");
                    int processSize = scanner.nextInt();

                    memoryManager.allocate(processID, processSize, method);
                    processID++;  // 进程ID递增
                    break;

                case 3:  // 回收内存
                    System.out.print("请输入回收的进程ID: ");
                    int deallocateProcessID = scanner.nextInt();
                    memoryManager.deallocate(deallocateProcessID);
                    break;

                case 4:  // 退出
                    System.out.println("退出程序.");
                    scanner.close();
                    return;

                case 5:  // 显示所有进程
                    memoryManager.displayProcesses();
                    break;

                case 6:  // 显示空闲内存区域表
                    memoryManager.displayFreeMemoryBlocks();
                    break;

                default:
                    System.out.println("无效的操作选项。");
                    break;
            }
        }
    }
}
/*示例 20大小

*最佳：分配10 8 释放1 再分配1
*最坏：同
* */

