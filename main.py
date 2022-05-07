import json
import subprocess
import time

import matplotlib.pyplot as plt
import numpy as np


def main():
    result = {}
    with open("gui-config.json", "r") as f:
        gui_config = json.load(f)
    plt.ion()
    ax = []
    ay = []
    for n in range(3):
        print("loop {}".format(n + 1))
        for i in gui_config["configs"]:
            url = i["server"]
            if n != 0:
                if url.split(".")[0] not in ax:
                    continue
            time = test_one(url)
            if url not in result:
                result[url] = []
            result[url].append(time)
            for r in result:
                if 0.0 not in result[r]:
                    ax.append(r.split(".")[0])
                    ay.append(np.mean(result[r]))

            plt.clf()  # 清除之前画的图
            plt.xticks(rotation=45)
            plt.bar(ax, ay)  # 画出当前 ax 列表和 ay 列表中的值的图形
            plt.pause(0.1)  # 暂停一秒
            plt.savefig("1.png")
    plt.ioff()


def test_one(url):
    print("testing {}".format(url))
    with open("config.json", "r") as f:
        config = json.load(f)
    config["remote_addr"] = url
    with open("new_config.json", "w") as f:
        json.dump(config, f)
    print("killing old trojan...")
    subprocess.run(["killall", "trojan"])
    time.sleep(1)
    print("running new trojan...")
    subprocess.Popen(["./trojan", "-c", "new_config.json"])
    time.sleep(1)
    p = subprocess.run(
        ["curl", "-4", "-o", "/dev/null", "--max-time", "3", "-s", "-w", "'%{time_starttransfer}'", "--socks5",
         "127.0.0.1:1080", "https://www.youtube.com"], capture_output=True)
    print(p.stdout.decode("utf-8"))
    a = float(p.stdout.decode("utf-8").replace("'", ""))
    return a


if __name__ == '__main__':
    main()
