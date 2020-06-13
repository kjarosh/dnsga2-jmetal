import csv
import matplotlib.pyplot as plt


def main():
    x = []
    y = []

    with open('FUN.tsv', 'r') as file:
        plots = csv.reader(file, delimiter=' ')
        for row in plots:
            x.append(float(row[0]))
            y.append(float(row[1]))

    plt.plot(x, y, marker='o', linestyle='None')

    plt.title('FUN')

    plt.xlabel('X')
    plt.ylabel('Y')

    plt.show()


if __name__ == '__main__':
    main()
