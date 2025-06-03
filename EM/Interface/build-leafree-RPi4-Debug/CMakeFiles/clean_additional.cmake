# Additional clean files
cmake_minimum_required(VERSION 3.16)

if("${CONFIG}" STREQUAL "" OR "${CONFIG}" STREQUAL "Debug")
  file(REMOVE_RECURSE
  "CMakeFiles/leafree_autogen.dir/AutogenUsed.txt"
  "CMakeFiles/leafree_autogen.dir/ParseCache.txt"
  "leafree_autogen"
  )
endif()
