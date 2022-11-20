import struct
from typing import List, Tuple

HEIGHT_MAP_SIZE = (1025, 1025)


class Obstacle:

    def __init__(self, point_list: List[Tuple[int]]):
        self.point_list = point_list

    def to_bytes(self) -> bytes:
        data = struct.pack("<i", len(self.point_list))

        for point in self.point_list:
            point_data = struct.pack("<ff", point[0], point[1])

            data += struct.pack("<i", len(point_data)) + point_data

        return data


class MapData:

    def __init__(self, height_map: List[List[int]], obstacles: List[Obstacle], water_level: float):
        self.height_map = height_map
        self.water_level = water_level
        self.obstacles = obstacles

    def write_to_file(self, map_name):
        if len(self.height_map) != HEIGHT_MAP_SIZE[0] or len(self.height_map[0]) != HEIGHT_MAP_SIZE[1]:
            raise Exception("Invalid heightmap size")

        with open("%s.map" % map_name, 'wb') as map_data:
            map_data.write(struct.pack("<fhh", self.water_level, HEIGHT_MAP_SIZE[0], HEIGHT_MAP_SIZE[1]))

            for x in range(HEIGHT_MAP_SIZE[0]):
                for y in range(HEIGHT_MAP_SIZE[1]):
                    map_data.write(struct.pack("<f", self.height_map[x][y]))

            for obstacle in self.obstacles:
                map_data.write(obstacle.to_bytes())


def read_hmap(hmap_file):
    h_map = [[0] * HEIGHT_MAP_SIZE[1]] * HEIGHT_MAP_SIZE[1]

    struct_fmt = '<e'
    struct_len = struct.calcsize(struct_fmt)
    struct_unpack = struct.Struct(struct_fmt).unpack_from

    with open(hmap_file, 'rb') as hmap_data:
        for x in range(HEIGHT_MAP_SIZE[0]):
            for y in range(HEIGHT_MAP_SIZE[1]):
                data = hmap_data.read(struct_len)
                h_map[x][y] = struct_unpack(data)[0]

    return h_map


if __name__ == "__main__":
    h_map = read_hmap("terrain.raw")

    m = MapData(h_map, obstacles=[], water_level=0)
    m.write_to_file("Spawn")
