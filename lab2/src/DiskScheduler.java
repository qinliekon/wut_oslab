import java.util.*;

class DiskScheduler {

    private int[] requests;  // 磁道访问请求序列
    private int head;        // 当前磁头位置
    private String direction;  // 磁头的移动方向

    // 构造函数
    public DiskScheduler(int[] requests, int head, String direction) {
        this.requests = Arrays.copyOf(requests, requests.length);  // 磁道请求
        this.head = head;  // 当前磁头位置
        this.direction = direction;  // 磁头移动方向
    }

    // 先来先服务法（FCFS）
    public int[] fcfs() {
        int seekCount = 0;
        List<Integer> sequence = new ArrayList<>(head);
        int currentPos = head;

        sequence.add(head);

        for (int request : requests) {
            seekCount += Math.abs(request - currentPos);
            sequence.add(request);
            currentPos = request;
        }

        System.out.println("FCFS调度:");
        System.out.println("访问顺序: " + sequence);
        System.out.println("总磁道移动数: " + seekCount);

        return new int[]{seekCount, sequence.size()};
    }

    // 最短寻道时间优先法（SSTF）
    public int[] sstf() {
        int seekCount = 0;
        List<Integer> sequence = new ArrayList<>(head);
        List<Integer> requestList = new ArrayList<>();
        for (int request : requests) {
            requestList.add(request);
        }

        sequence.add(head);

        int currentPos = head;
        while (!requestList.isEmpty()) {
            // 找到距离当前磁头位置最近的请求
            int nearestRequest = requestList.get(0);
            int minDistance = Math.abs(nearestRequest - currentPos);

            for (int request : requestList) {
                int distance = Math.abs(request - currentPos);
                if (distance < minDistance) {
                    nearestRequest = request;
                    minDistance = distance;
                }
            }

            seekCount += minDistance;
            sequence.add(nearestRequest);
            currentPos = nearestRequest;
            requestList.remove(Integer.valueOf(nearestRequest));
        }

        System.out.println("\nSSTF调度:");
        System.out.println("访问顺序: " + sequence);
        System.out.println("总磁道移动数: " + seekCount);

        return new int[]{seekCount, sequence.size()};
    }

    // 电梯算法（SCAN）
    public int[] scan() {
        int seekCount = 0;
        List<Integer> sequence = new ArrayList<>();
        List<Integer> left = new ArrayList<>();
        List<Integer> right = new ArrayList<>();

        sequence.add(head);

        // 将请求分为左侧和右侧
        for (int request : requests) {
            if (request < head) {
                left.add(request);
            } else {
                right.add(request);
            }
        }

        // 对左侧和右侧的请求分别排序
        Collections.sort(left, Collections.reverseOrder()); // 降序
        Collections.sort(right);

        int currentPos = head;
        if ("right".equals(direction)) {
            // 向右扫描
            sequence.addAll(right);
            sequence.addAll(left);
        } else if ("left".equals(direction)) {
            // 向左扫描
            sequence.addAll(left);
            sequence.addAll(right);
        }

        // 计算总磁道移动
        for (int i = 1; i < sequence.size(); i++) {
            seekCount += Math.abs(sequence.get(i) - sequence.get(i - 1));
        }

        System.out.println("\nSCAN调度:");
        System.out.println("访问顺序: " + sequence);
        System.out.println("总磁道移动数: " + seekCount);

        return new int[]{seekCount, sequence.size()};
    }

    // 显示菜单
    public static void showMenu() {
        System.out.println("\n--- 磁盘调度算法模拟器 ---");
        System.out.println("1. 先来先服务法（FCFS）");
        System.out.println("2. 最短寻道时间优先（SSTF）");
        System.out.println("3. 电梯算法（SCAN）");
        System.out.println("4. 退出");
        System.out.print("请选择一个选项: ");
    }

    // 主方法
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            // 显示菜单
            showMenu();
            int choice = scanner.nextInt();

            if (choice == 4) {
                System.out.println("程序退出.");
                scanner.close();
                return;
            }

            // 用户输入磁头位置、请求序列和磁头移动方向
            System.out.print("请输入磁头位置: ");
            int head = scanner.nextInt();

            System.out.print("请输入磁头的移动方向（left 或 right）: ");
            String direction = scanner.next();

            System.out.print("请输入磁道请求序列（以空格分隔）: ");
            scanner.nextLine();  // 清除换行符
            String[] requestInput = scanner.nextLine().split(" ");
            int[] requests = new int[requestInput.length];
            for (int i = 0; i < requestInput.length; i++) {
                requests[i] = Integer.parseInt(requestInput[i]);
            }

            // 创建DiskScheduler对象
            DiskScheduler scheduler = new DiskScheduler(requests, head, direction);

            switch (choice) {
                case 1:
                    scheduler.fcfs();
                    break;
                case 2:
                    scheduler.sstf();
                    break;
                case 3:
                    scheduler.scan();
                    break;
                default:
                    System.out.println("无效的选项，请重新选择。");
            }
        }
    }
}
